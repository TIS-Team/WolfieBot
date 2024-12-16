package pl.tispmc.wolfie.common.service;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.tispmc.wolfie.common.mapper.EvaluationUserMapper;
import pl.tispmc.wolfie.common.model.Evaluation;
import pl.tispmc.wolfie.common.model.EvaluationId;
import pl.tispmc.wolfie.common.model.User;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.model.UserId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserEvaluationService
{
    private static final Map<EvaluationId, Evaluation> EVALUATIONS = new HashMap<>();
    private static final ConcurrentLinkedQueue<EvaluationId> RESERVED_EVALUATION_IDS = new ConcurrentLinkedQueue<>();

    private final UserDataService userDataService;
    private final EvaluationUserMapper evaluationUserMapper;

    @Scheduled(initialDelay = 5, fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void clearReservedEvaluationIds()
    {
        RESERVED_EVALUATION_IDS.clear();
    }

    public Evaluation generateEvaluation(List<User> players, User missionMaker, List<User> gameMasters)
    {
        UUID uuid = generateEvaluationId();
        final Evaluation evaluation = new Evaluation();
        evaluation.setId(uuid);

        Map<UserId, UserData> userDataMap = userDataService.findAll();

        final List<Evaluation.EvaluationUser> evaluationPlayers = players.stream()
                .map(user -> asEvaluatonUser(user, userDataMap))
                .toList();
        evaluation.setPlayers(evaluationPlayers);

        evaluation.setMissionMaker(Optional.ofNullable(missionMaker)
                .map(user -> asEvaluatonUser(user, userDataMap))
                .orElse(null));

        final List<Evaluation.EvaluationUser> evaluationGameMasters = gameMasters.stream()
                .map(user -> asEvaluatonUser(user, userDataMap))
                .toList();
        evaluation.setGameMasters(evaluationGameMasters);


        EvaluationId evaluationId = EvaluationId.of(uuid);
        RESERVED_EVALUATION_IDS.remove(evaluationId);
        EVALUATIONS.put(evaluationId, evaluation);
        return evaluation;
    }

    public Evaluation findEvaluation(UUID evaluationId)
    {
        return EVALUATIONS.get(EvaluationId.of(evaluationId));
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

    private Evaluation.EvaluationUser asEvaluatonUser(User user, Map<UserId, UserData> userDataMap)
    {
        return Optional.ofNullable(user)
                .map(u -> UserWithUserData.of(user, userDataMap.get(UserId.of(u.getId()))))
                .map(u -> this.evaluationUserMapper.map(u.getUser(), u.getUserData()))
                .orElse(null);
    }

    @Value(staticConstructor = "of")
    private static class UserWithUserData
    {
        User user;
        UserData userData;
    }
}
