package pl.tispmc.wolfie.discord.command.validation;

import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SlashCommandPrerequisites
{
    @Value("${bot.roles.game-master.id}")
    private String gameMasterRoleId;

    public boolean hasGameMasterRole(Member member)
    {
        return Optional.ofNullable(member)
                .map(Member::getRoles)
                .map(roles -> roles.stream()
                        .anyMatch(role -> role.getId().equals(gameMasterRoleId)))
                .orElse(false);
    }
}
