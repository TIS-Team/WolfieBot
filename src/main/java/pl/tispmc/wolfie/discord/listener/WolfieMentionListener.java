package pl.tispmc.wolfie.discord.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.config.GeminiConfig;
import pl.tispmc.wolfie.discord.service.WolfieMentionService;

@Component
@RequiredArgsConstructor
@Slf4j
public class WolfieMentionListener extends ListenerAdapter {

    private final GeminiConfig geminiConfig;
    private final WolfieMentionService wolfieMentionService;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (geminiConfig.getChannelId() != null && !event.getChannel().getId().equals(geminiConfig.getChannelId())) {
            return;
        }

        wolfieMentionService.handleMention(event);
    }
}