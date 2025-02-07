package pl.tispmc.wolfie.common.event.model;

import org.springframework.context.ApplicationEvent;
import pl.tispmc.wolfie.common.model.EvaluationSummary;

public class EvaluationSummaryEvent extends ApplicationEvent
{
    private final EvaluationSummary evaluationSummary;

    public EvaluationSummaryEvent(Object source, EvaluationSummary evaluationSummary)
    {
        super(source);
        this.evaluationSummary = evaluationSummary;
    }

    public EvaluationSummary getEvaluationSummary()
    {
        return evaluationSummary;
    }
}
