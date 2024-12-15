package pl.tispmc.wolfie.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/evaluation")
public class EvaluationController
{
    @GetMapping("/**")
    public String evaluationPage()
    {
        return "/evaluation.html";
    }
}
