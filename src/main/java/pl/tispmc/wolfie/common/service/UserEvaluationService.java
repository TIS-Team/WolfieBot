package pl.tispmc.wolfie.common.service;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.tispmc.wolfie.common.exception.EvaluationNotFoundException;
import pl.tispmc.wolfie.common.mapper.EvaluationUserMapper;
import pl.tispmc.wolfie.common.model.Action;
import pl.tispmc.wolfie.common.model.Evaluation;
import pl.tispmc.wolfie.common.model.EvaluationId;
import pl.tispmc.wolfie.common.model.EvaluationSubmission;
import pl.tispmc.wolfie.common.model.User;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.model.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

    @Scheduled(initialDelay = 5, fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void clearForgottenEvaluations()
    {
        if (generationInProgress.get())
            return;

        RESERVED_EVALUATION_IDS.clear();

        LocalDateTime now = LocalDateTime.now();

        boolean hasRemovedOldEvaluations = EVALUATIONS.entrySet().removeIf(entry -> entry.getValue().getCreatedDate().isBefore(now.minusDays(1)));
        if (hasRemovedOldEvaluations) {
            log.info("Removed old evaluations.");
        }
    }

    public void submitEvaluation(UUID evaluationId, EvaluationSubmission evaluationSubmission)
    {
        log.info("Evaluation with id " + evaluationId + " has been submitted!");
        Evaluation evaluation = findEvaluation(evaluationId);
        if (evaluation == null){
            log.warn("Evaluation with id " + evaluationId + " not found");
            throw new EvaluationNotFoundException("Evaluation with id " + evaluationId + " not found");
        }

        List<Long> evaluationUserIds = evaluation.getAllEvaluationUsers().stream()
                .map(Evaluation.EvaluationUser::getId)
                .toList();

        List<UserData> existingUserDatas = userDataService.findAll().entrySet()
                .stream()
                .filter(entry -> evaluationUserIds.contains(entry.getKey().getId()))
                .map(Map.Entry::getValue)
                .toList();

        Map<UserId, Integer> expUserChange = calculateExpChangePerUser(evaluationSubmission);

        List<UserData> updatedUserDatas = existingUserDatas.stream()
                .map(userData -> userData.toBuilder().exp(userData.getExp() + expUserChange.getOrDefault(UserId.of(userData.getUserId()), 0)).build())
                .toList();

        this.userDataService.save(updatedUserDatas);
        log.info("Evaluation with id " + evaluationId + " has been completed!");
        clearEvaluation(evaluationId);
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
        log.info("Evaluation with id " + evaluationId + " has been cleared!");
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
                .orElse(null);
    }

    private Map<UserId, Integer> calculateExpChangePerUser(EvaluationSubmission evaluationSubmission)
    {
        return evaluationSubmission.getUsers().stream().collect(Collectors.toMap(
                submittedUser -> UserId.of(submittedUser.getUserId()),
                submittedUser -> submittedUser.getActions().stream()
                        .map(Action::getValue)
                        .reduce(0, Integer::sum))
        );
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
