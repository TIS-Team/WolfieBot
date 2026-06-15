package pl.tispmc.wolfie.discord.listener;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuditLogListener extends ListenerAdapter
{
    @Override
    public void onGuildAuditLogEntryCreate(@NonNull GuildAuditLogEntryCreateEvent event)
    {
        logAuditLogCreateEvent(event);
    }

    private void logAuditLogCreateEvent(@NonNull GuildAuditLogEntryCreateEvent event)
    {
        User user = getUser(event.getJDA(), event.getEntry().getUserIdLong());
        log.info("[AUDIT_LOG]: guild:{}:{}, user:{}:{}, actions: {}",
                event.getGuild().getId(),
                event.getGuild().getName(),
                Optional.ofNullable(user).map(ISnowflake::getId).orElse(null),
                Optional.ofNullable(user).map(User::getName).orElse(null),
                convertWithStream(event.getEntry().getChanges()));
    }

    private User getUser(JDA jda, long userId)
    {
        return jda.getUserById(userId);
    }

    private String convertWithStream(Map<?, ?> map) {
        return map.keySet().stream()
                .map(key -> key + "=" + map.get(key))
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
