package pl.tispmc.wolfie.discord.application.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.event.model.DisplayExpChangeEvent;
import pl.tispmc.wolfie.discord.service.DiscordExpChangeMessagePublisher;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisplayExpChangeEventListener
{
    private final DiscordExpChangeMessagePublisher publisher;

    @Async
    @EventListener
    public void onDisplayExpChangeEvent(DisplayExpChangeEvent event)
    {
        publisher.publishMessage(event.getParams());
    }
}
