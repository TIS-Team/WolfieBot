package pl.tispmc.wolfie.discord.listener;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.service.SilentChannelService;

@Component
@Slf4j
public class SilentChannelMessageListener extends ListenerAdapter
{
    private final SilentChannelService silentChannelService;
    private final String silentChannelId;

    public SilentChannelMessageListener(SilentChannelService silentChannelService,
                                        @Value("${bot.channels.silent-channel.id:0}") String silentChannelId)
    {
        this.silentChannelService = silentChannelService;
        this.silentChannelId = silentChannelId;
    }

    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event)
    {
        if (event.getJDA().getSelfUser().getId().equals(event.getAuthor().getId()))
            return;

        if (!event.isFromGuild())
            return;

        if (!event.getGuildChannel().getId().equals(silentChannelId))
            return;

        Member member = event.getMember();
        if (member == null)
            return;

        this.silentChannelService.handleSilentChannelMessage(event.getGuildChannel(), event.getMessage(), member);
    }
}
