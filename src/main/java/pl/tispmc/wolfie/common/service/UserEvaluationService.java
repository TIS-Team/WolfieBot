package pl.tispmc.wolfie.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.tispmc.wolfie.common.mapper.EvaluationUserMapper;
import pl.tispmc.wolfie.common.model.Evaluation;
import pl.tispmc.wolfie.common.model.EvaluationId;
import pl.tispmc.wolfie.common.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserEvaluationService
{
    private static final Map<EvaluationId, Evaluation> EVALUATIONS = new HashMap<>();
    private static final ConcurrentLinkedQueue<EvaluationId> RESERVED_EVALUATION_IDS = new ConcurrentLinkedQueue<>();

    private final EvaluationUserMapper evaluationUserMapper;

    @Scheduled(initialDelay = 5, fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void clearReservedEvaluationIds()
    {
        RESERVED_EVALUATION_IDS.clear();
    }

    public Evaluation generateEvaluation(List<User> players, User missionMaker, List<User> gameMasters)
    {
        UUID uuid = UUID.randomUUID();
        final Evaluation evaluation = new Evaluation();
        evaluation.setId(uuid);
        evaluation.setPlayers(players.stream().map(evaluationUserMapper::map).toList());
        evaluation.setMissionMaker(evaluationUserMapper.map(missionMaker));
        evaluation.setGameMasters(gameMasters.stream().map(evaluationUserMapper::map).toList());


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
}
