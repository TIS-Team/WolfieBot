package pl.tispmc.wolfie.web.rest;

import lombok.RequiredArgsConstructor;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/evaluation")
@RequiredArgsConstructor
public class EvaluationRestController
{
    private final UserEvaluationService userEvaluationService;

    @GetMapping("/{evaluationId}")
    public ResponseEntity<Evaluation> getEvaluation(@PathVariable("evaluationId") UUID evaluationId)
    {
        Evaluation evaluation = userEvaluationService.findEvaluation(evaluationId);
        if (evaluation == null)
            return ResponseEntity.notFound().build();
        else return ResponseEntity.ok(evaluation);
    }

    @PostMapping("/{evaluationId}")
    public void submitEvaluation(@PathVariable("evaluationId") UUID evaluationId, @RequestBody EvaluationSubmission evaluationSubmission)
    {
        userEvaluationService.submitEvaluation(evaluationId, evaluationSubmission);
    }

    @DeleteMapping("{evaluationId}")
    public void deleteEvaluation(@PathVariable("evaluationId") UUID evaluationId)
    {
        userEvaluationService.deleteEvaluation(evaluationId);
    }

    @ExceptionHandler({EvaluationNotFoundException.class})
    public ResponseEntity<?> onEvaluationNotFound(EvaluationNotFoundException exception)
    {
        return ResponseEntity.badRequest().body(RestErrorResponse.of(exception.getMessage()));
    }
}
