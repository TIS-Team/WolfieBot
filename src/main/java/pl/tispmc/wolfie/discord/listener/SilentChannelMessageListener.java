package pl.tispmc.wolfie.discord.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class SilentChannelMessageListener extends ListenerAdapter
{
    @Value("${bot.channels.silent-channel.id:0}")
    private String silentChannelId;

    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event)
    {
        if (!event.isFromGuild())
            return;

        if (!event.getChannel().getId().equals(silentChannelId))
            return;

        Member member = event.getMember();
        log.info("Silent channel - Banning user: {} {}", member.getUser().getName(), member.getEffectiveName());
        log.info("Silent channel - Deleting 1h timeframe messages for user: {} {}", member.getUser().getName(), member.getEffectiveName());
        try
        {
            event.getChannel().sendMessage("Wynocha stąd " + member.getAsMention() + "!").queue();
            member.ban(1, TimeUnit.HOURS).reason("Pisanie na zakazanym kanale!").queue();
        }
        catch (Exception e)
        {
            log.error("Could not ban the given user", e);
        }
    }
}
