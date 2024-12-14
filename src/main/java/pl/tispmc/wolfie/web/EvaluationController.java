package pl.tispmc.wolfie.web;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.tispmc.wolfie.common.model.Evaluation;
import pl.tispmc.wolfie.common.service.UserEvaluationService;

import java.util.UUID;

@Controller
@RequestMapping("/evaluation")
@AllArgsConstructor
public class EvaluationController
{
    private final UserEvaluationService userEvaluationService;

    @GetMapping("/{evaluationId}")
    public ResponseEntity<String> getEvaluationPage(@PathVariable("evaluationId") UUID evaluationId)
    {
        Evaluation evaluation = userEvaluationService.findEvaluation(evaluationId);
        if (evaluation == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(prepareEvaluationPage(evaluation));
    }

    private String prepareEvaluationPage(Evaluation evaluation)
    {
        return "<html></html><body>" + evaluation + "</body></html>";
    }
}
