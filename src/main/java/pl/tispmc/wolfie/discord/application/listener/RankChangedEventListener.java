package pl.tispmc.wolfie.discord.application.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.event.model.RankChangedEvent;
import pl.tispmc.wolfie.discord.service.RankChangedMessagePublisher;

@Component
@Slf4j
@AllArgsConstructor
public class RankChangedEventListener
{
    private final RankChangedMessagePublisher rankChangedMessagePublisher;

    @Async
    @EventListener
    public void onRankChange(RankChangedEvent event)
    {
        log.info("Rank changed event: {}", event);
        rankChangedMessagePublisher.publish(event.getUserName(), event.getOldRank(), event.getNewRank());
    }
}
