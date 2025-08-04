package pl.tispmc.wolfie.discord.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.service.JoinRolesService;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildJoinEventListener extends ListenerAdapter
{
    private final JoinRolesService joinRolesService;

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event)
    {
        joinRolesService.setJoinRoles(event.getMember());
    }
}
