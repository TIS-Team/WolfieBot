package pl.tispmc.wolfie.discord.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Service;
import pl.tispmc.wolfie.discord.config.JoinRolesConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class JoinRolesService
{
    private final JoinRolesConfigurationProperties joinRolesConfigurationProperties;

    public void setJoinRoles(Member member)
    {
        Guild guild = member.getGuild();
        List<Role> rolesToAdd = guild.getRoles().stream()
                .filter(role -> joinRolesConfigurationProperties.getAdd().contains(role.getIdLong()))
                .toList();

        logSettingJoinRoles(rolesToAdd, member);
        member.getGuild().modifyMemberRoles(member, rolesToAdd, List.of()).queue();
    }

    private void logSettingJoinRoles(List<Role> rolesToAdd, Member member)
    {
        if (log.isInfoEnabled())
        {
            log.info("Setting join roles {} for member id = '{}', name = '{}'", Arrays.toString(rolesToAdd.toArray()), member.getId(), member.getEffectiveName());
        }
    }
}
