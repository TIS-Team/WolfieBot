package pl.tispmc.wolfie.common.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class Evaluation
{
    private UUID id;
    private List<EvaluationUser> players;
    private EvaluationUser missionMaker;
    private List<EvaluationUser> gameMasters;

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
