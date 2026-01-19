package pl.tispmc.wolfie.web.rest;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.tispmc.wolfie.common.exception.EvaluationNotFoundException;
import pl.tispmc.wolfie.common.model.Evaluation;
import pl.tispmc.wolfie.common.model.EvaluationSubmission;
import pl.tispmc.wolfie.common.service.UserEvaluationService;
import pl.tispmc.wolfie.web.model.RestErrorResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/evaluation")
@RequiredArgsConstructor
public class EvaluationRestController
{
    private final UserEvaluationService userEvaluationService;

    @GetMapping("/{evaluationId}")
    public ResponseEntity<EvaluationResponse> getEvaluation(@PathVariable("evaluationId") UUID evaluationId)
    {
        Evaluation evaluation = userEvaluationService.findEvaluation(evaluationId);
        if (evaluation == null)
            return ResponseEntity.notFound().build();
        else return ResponseEntity.ok(toResponse(evaluation));
    }

    @PostMapping("/{evaluationId}")
    public void submitEvaluation(@PathVariable("evaluationId") UUID evaluationId, @RequestBody EvaluationSubmission evaluationSubmission)
    {
        userEvaluationService.submitEvaluation(evaluationId, evaluationSubmission);
    }

    @DeleteMapping("/{evaluationId}")
    public void deleteEvaluation(@PathVariable("evaluationId") UUID evaluationId)
    {
        userEvaluationService.deleteEvaluation(evaluationId);
    }

    @ExceptionHandler({EvaluationNotFoundException.class})
    public ResponseEntity<?> onEvaluationNotFound(EvaluationNotFoundException exception)
    {
        return ResponseEntity.badRequest().body(RestErrorResponse.of(exception.getMessage()));
    }

    private EvaluationResponse toResponse(Evaluation evaluation)
    {
        return EvaluationResponse.builder()
                .id(evaluation.getId())
                .missionMaker(toEvaluationResponseUser(evaluation.getMissionMaker()))
                .players(evaluation.getPlayers().stream().map(this::toEvaluationResponseUser).toList())
                .gameMasters(evaluation.getGameMasters().stream().map(this::toEvaluationResponseUser).toList())
                .createdDate(evaluation.getCreatedDate())
                .build();
    }

    private EvaluationResponse.EvaluationResponseUser toEvaluationResponseUser(Evaluation.EvaluationUser evaluationUser)
    {
        return EvaluationResponse.EvaluationResponseUser.builder()
                .id(String.valueOf(evaluationUser.getId()))
                .name(evaluationUser.getName())
                .avatarUrl(evaluationUser.getAvatarUrl())
                .exp(evaluationUser.getExp())
                .build();
    }

    @Builder
    @Value
    @AllArgsConstructor
    public static class EvaluationResponse
    {
        UUID id;
        List<EvaluationResponseUser> players;
        EvaluationResponseUser missionMaker;
        List<EvaluationResponseUser> gameMasters;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdDate;

        @Data
        @Builder
        public static class EvaluationResponseUser
        {
            private String id;
            private String name;
            private String avatarUrl;
            private int exp;
        }
    }
}
