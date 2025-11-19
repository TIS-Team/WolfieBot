package pl.tispmc.wolfie.discord.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.service.WolfieMentionService;

@Component
@RequiredArgsConstructor
@Slf4j
public class WolfieMentionListener extends ListenerAdapter
{
    private final WolfieMentionService wolfieMentionService;

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) {
            log.debug("Ignoring message from bot: {}", event.getAuthor().getName());
            return;
        }

        boolean isMentioned = event.getMessage().getMentions().getUsers().stream()
                .anyMatch(user -> user.getId().equals(event.getJDA().getSelfUser().getId()));

        if (!isMentioned)
            return;

        wolfieMentionService.handleMention(event);
    }
}