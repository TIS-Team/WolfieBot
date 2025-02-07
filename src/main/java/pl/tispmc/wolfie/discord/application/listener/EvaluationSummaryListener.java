package pl.tispmc.wolfie.discord.application.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.event.model.EvaluationSummaryEvent;
import pl.tispmc.wolfie.discord.service.EvaluationSummaryMessagePublisher;

@Slf4j
@AllArgsConstructor
@Component
public class EvaluationSummaryListener
{
    private final EvaluationSummaryMessagePublisher publisher;

    @Async
    @EventListener
    public void onUpdateUserRoles(EvaluationSummaryEvent event)
    {
        log.info("Evaluation summary event: {}", event);
        this.publisher.publish(event.getEvaluationSummary());
    }
}
