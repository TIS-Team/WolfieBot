package pl.tispmc.wolfie.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Data
public class Evaluation
{
    private UUID id;
    private List<EvaluationUser> players;
    private EvaluationUser missionMaker;
    private List<EvaluationUser> gameMasters;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdDate;

    public List<EvaluationUser> getAllEvaluationUsers()
    {
        return Stream.of(players, gameMasters, List.of(missionMaker))
                .flatMap(Collection::stream)
                .toList();
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
