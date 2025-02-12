package pl.tispmc.wolfie.common.service;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.tispmc.wolfie.common.dto.SubmittedUser;
import pl.tispmc.wolfie.common.event.model.EvaluationSummaryEvent;
import pl.tispmc.wolfie.common.event.model.UpdateUserRolesEvent;
import pl.tispmc.wolfie.common.exception.EvaluationNotFoundException;
import pl.tispmc.wolfie.common.mapper.EvaluationUserMapper;
import pl.tispmc.wolfie.common.model.Action;
import pl.tispmc.wolfie.common.model.Evaluation;
import pl.tispmc.wolfie.common.model.EvaluationId;
import pl.tispmc.wolfie.common.model.EvaluationSubmission;
import pl.tispmc.wolfie.common.model.EvaluationSummary;
import pl.tispmc.wolfie.common.model.User;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.model.UserId;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEvaluationService
{
    private static final Map<EvaluationId, Evaluation> EVALUATIONS = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<EvaluationId> RESERVED_EVALUATION_IDS = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean generationInProgress = new AtomicBoolean(false);

    private final UserDataService userDataService;
    private final EvaluationUserMapper evaluationUserMapper;

    private final ApplicationEventPublisher eventPublisher;

    @org.springframework.beans.factory.annotation.Value("${evaluation.expiration-time}")
    private Duration evaluationExpirationTime;

    @Scheduled(initialDelay = 5, fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void clearForgottenEvaluations()
    {
        if (generationInProgress.get())
            return;

        removeOldEvaluations();
    }

    private void removeOldEvaluations()
    {
        RESERVED_EVALUATION_IDS.clear();
        LocalDateTime now = LocalDateTime.now();

        List<UUID> evaluationsToRemove = new LinkedList<>();
        for (Evaluation evaluation : EVALUATIONS.values())
        {
            if (evaluation.getCreatedDate().isBefore(now.minus(evaluationExpirationTime)))
            {
                evaluationsToRemove.add(evaluation.getId());
            }
        }

        if (evaluationsToRemove.isEmpty())
            return;

        EVALUATIONS.values().removeIf(evaluation -> evaluationsToRemove.contains(evaluation.getId()));
        log.info("Removed old evaluations: {}", evaluationsToRemove);
    }

    public void submitEvaluation(UUID evaluationId, EvaluationSubmission evaluationSubmission)
    {
        log.info("Evaluation with id {} has been submitted!", evaluationId);
        Evaluation evaluation = findEvaluation(evaluationId);
        if (evaluation == null){
            log.warn("Evaluation with id {} not found", evaluationId);
            throw new EvaluationNotFoundException("Evaluation with id " + evaluationId + " not found");
        }

        Map<UserId, SubmittedUser> submittedUsers = evaluationSubmission.getUsers().stream()
                .collect(Collectors.toMap(user -> UserId.of(Long.parseLong(user.getUserId())), user -> user));
        Map<UserId, Integer> userExpChanges = calculateExpChangePerUser(submittedUsers);
        Map<Long, Evaluation.EvaluationUser> evaluationUsers = evaluation.getPlayers().stream()
                .collect(Collectors.toMap(Evaluation.EvaluationUser::getId, Function.identity()));
        Map<UserId, UserData> existingUserDatas = userDataService.findAll();

        Map<UserId, UserData> updatedUserDatas = evaluationUsers.values().stream()
                .map(evaluationUser -> updateUserData(evaluationUser, userExpChanges, submittedUsers, existingUserDatas))
                .collect(Collectors.toMap(data -> UserId.of(data.getUserId()), userData -> userData));

        this.userDataService.save(updatedUserDatas.values());
        log.info("Evaluation with id {} has been completed!", evaluationId);
        clearEvaluation(evaluationId);

        EvaluationSummary evaluationSummary = prepareEvaluationSummary(
                evaluationSubmission.getMissionName(),
                submittedUsers,
                evaluationUsers,
                evaluation.getMissionMaker(),
                updatedUserDatas,
                userExpChanges
        );

        eventPublisher.publishEvent(new EvaluationSummaryEvent(this, evaluationSummary));
        eventPublisher.publishEvent(new UpdateUserRolesEvent(this, evaluationUsers.keySet()));
    }

    private UserData updateUserData(Evaluation.EvaluationUser evaluationUser,
                                    Map<UserId, Integer> userExpChanges,
                                    Map<UserId, SubmittedUser> submittedUsers,
                                    Map<UserId, UserData> existingUserDatas)
    {
        UserData userData = existingUserDatas.getOrDefault(UserId.of(evaluationUser.getId()), UserData.builder()
                .userId(evaluationUser.getId())
                .name(evaluationUser.getName())
                .build());

        int newAppraisals = (int)submittedUsers.get(UserId.of(userData.getUserId())).getActions().stream()
                .map(Action::getValue)
                .filter(value -> value > 0)
                .count();
        int newReprimands = (int)submittedUsers.get(UserId.of(userData.getUserId())).getActions().stream()
                .map(Action::getValue)
                .filter(value -> value < 0)
                .count();

        return userData.toBuilder()
                .exp(userData.getExp() + userExpChanges.getOrDefault(UserId.of(userData.getUserId()), 0))
                .appraisalsCount(userData.getAppraisalsCount() + newAppraisals)
                .reprimandsCount(userData.getReprimandsCount() + newReprimands)
                .missionsPlayed(userData.getMissionsPlayed() + 1)
                .build();
    }

    private static EvaluationSummary prepareEvaluationSummary(String missionName,
                                                       Map<UserId, SubmittedUser> submittedUsers,
                                                       Map<Long, Evaluation.EvaluationUser> evaluationUsers,
                                                       Evaluation.EvaluationUser missionMaker,
                                                       Map<UserId, UserData> updatedUserDatas,
                                                       Map<UserId, Integer> expChanges)
    {
        return EvaluationSummary.builder()
                .missionName(missionName)
                .missionMaker(EvaluationSummary.SummaryPlayer.builder()
                        .id(missionMaker.getId())
                        .name(missionMaker.getName())
                        .avatarUrl(missionMaker.getAvatarUrl())
                        .exp(Optional.ofNullable(updatedUserDatas.get(UserId.of(missionMaker.getId()))).map(UserData::getExp)
                                .orElse(missionMaker.getExp()))
                        .build())
                .players(updatedUserDatas.values().stream()
                        .map(userData -> EvaluationSummary.SummaryPlayer.builder()
                                .id(userData.getUserId())
                                .name(userData.getName())
                                .avatarUrl(evaluationUsers.get(userData.getUserId()).getAvatarUrl())
                                .expChange(expChanges.getOrDefault(UserId.of(userData.getUserId()), 0))
                                .exp(updatedUserDatas.get(UserId.of(userData.getUserId())).getExp())
                                .actions(submittedUsers.get(UserId.of(userData.getUserId())).getActions())
                                .missionsPlayed(updatedUserDatas.get(UserId.of(userData.getUserId())).getMissionsPlayed())
                                .build())
                        .toList())
                .build();
    }

    public Evaluation generateEvaluation(List<User> players, User missionMaker, List<User> gameMasters)
    {
        setGenerationInProgress(true);
        UUID uuid = generateEvaluationId();
        final Evaluation evaluation = new Evaluation();
        evaluation.setId(uuid);

        Map<UserId, UserData> userDataMap = userDataService.findAll();

        evaluation.setPlayers(players.stream()
                .map(user -> asEvaluationUser(user, userDataMap))
                .toList());

        evaluation.setMissionMaker(ofNullable(missionMaker)
                .map(user -> asEvaluationUser(user, userDataMap))
                .orElse(null));

        evaluation.setGameMasters(gameMasters.stream()
                .map(user -> asEvaluationUser(user, userDataMap))
                .toList());

        evaluation.setCreatedDate(LocalDateTime.now());
        EvaluationId evaluationId = EvaluationId.of(uuid);
        RESERVED_EVALUATION_IDS.remove(evaluationId);
        EVALUATIONS.put(evaluationId, evaluation);
        setGenerationInProgress(false);
        return evaluation;
    }

    public Evaluation findEvaluation(UUID evaluationId)
    {
        return EVALUATIONS.get(EvaluationId.of(evaluationId));
    }

    private void setGenerationInProgress(boolean value)
    {
        generationInProgress.set(value);
    }

    private void clearEvaluation(UUID evaluationId)
    {
        EVALUATIONS.remove(EvaluationId.of(evaluationId));
        log.info("Evaluation with id {} has been cleared!", evaluationId);
    }

    private UUID generateEvaluationId()
    {
        UUID uuid = UUID.randomUUID();
        EvaluationId evaluationId = EvaluationId.of(uuid);
        while (EVALUATIONS.containsKey(evaluationId) || RESERVED_EVALUATION_IDS.contains(evaluationId))
        {
            uuid = UUID.randomUUID();
        }
        RESERVED_EVALUATION_IDS.add(evaluationId);
        return uuid;
    }

    private Evaluation.EvaluationUser asEvaluationUser(User user, Map<UserId, UserData> userDataMap)
    {
        return ofNullable(user)
                .map(u -> UserWithUserData.of(user, userDataMap.get(UserId.of(u.getId()))))
                .map(u -> this.evaluationUserMapper.map(u.getUser(), u.getUserData()))
                .orElse(createNewEvaluationUser(user));
    }

    private Evaluation.EvaluationUser createNewEvaluationUser(User user)
    {
        return Evaluation.EvaluationUser.builder()
                .id(user.getId())
                .avatarUrl(user.getAvatarUrl())
                .name(user.getName())
                .exp(0)
                .build();
    }

    private Map<UserId, Integer> calculateExpChangePerUser(Map<UserId, SubmittedUser> submittedUsers)
    {
        return submittedUsers.values().stream()
                .collect(Collectors.toMap(userExpChange -> UserId.of(Long.parseLong(userExpChange.getUserId())), this::calculateExpChangePerUser));
    }


    private Integer calculateExpChangePerUser(SubmittedUser submittedUser)
    {
        return submittedUser.getActions().stream()
                .map(Action::getValue)
                .reduce(0, Integer::sum);
    }

    public void deleteEvaluation(UUID evaluationId)
    {
        clearEvaluation(evaluationId);
    }

    @Value(staticConstructor = "of")
    private static class UserWithUserData
    {
        User user;
        UserData userData;
    }
}
