package pl.tispmc.wolfie.common.event.model;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.context.ApplicationEvent;
import pl.tispmc.wolfie.common.model.EvaluationSummary;

@Value
@EqualsAndHashCode(callSuper = false)
public class EvaluationSummaryEvent extends ApplicationEvent
{
    EvaluationSummary evaluationSummary;

    public EvaluationSummaryEvent(Object source, EvaluationSummary evaluationSummary)
    {
        super(source);
        this.evaluationSummary = evaluationSummary;
    }
}
