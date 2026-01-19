package pl.tispmc.wolfie.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation
{
    private UUID id;
    private List<EvaluationUser> players;
    private EvaluationUser missionMaker;
    private List<EvaluationUser> gameMasters;

    private LocalDateTime createdDate;

    public List<EvaluationUser> getAllEvaluationUsers()
    {
        return Stream.of(players, gameMasters, List.of(missionMaker))
                .flatMap(Collection::stream)
                .toList();
    }

    public Map<Long, EvaluationUser> getAllEvaluationUsersAsMap()
    {
        return getAllEvaluationUsers().stream()
                .collect(Collectors.toMap(EvaluationUser::getId, Function.identity()));
    }

    @Data
    @Builder
    public static class EvaluationUser
    {
        private long id;
        private String name;
        private String avatarUrl;
        private int exp;
    }
}
