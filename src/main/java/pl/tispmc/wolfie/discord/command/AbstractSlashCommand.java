package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Optional;
import java.util.Set;

public abstract class AbstractSlashCommand implements SlashCommand
{
    protected static final String ALL_SUPPORTED = "*";

    private final Set<String> supportedChannelIds;
    private final Set<String> supportedRoleIds;

    protected AbstractSlashCommand(
            Set<String> supportedChannelIds,
            Set<String> supportedRoleIds)
    {
        this.supportedChannelIds = supportedChannelIds;
        this.supportedRoleIds = supportedRoleIds;
    }

    protected AbstractSlashCommand() {
        this(Set.of(ALL_SUPPORTED), Set.of(ALL_SUPPORTED));
    }

    @Override
    public boolean supports(SlashCommandInteractionEvent event)
    {
        return SlashCommand.super.supports(event);
    }

    @Override
    public boolean supportsChannel(String channelId)
    {
        return this.supportedChannelIds.contains(ALL_SUPPORTED)
                || this.supportedChannelIds.contains(channelId);
    }

    @Override
    public boolean supportsRole(String roleId)
    {
        return this.supportedRoleIds.contains(ALL_SUPPORTED)
                || this.supportedRoleIds.contains(roleId);
    }

    protected boolean hasRequiredRole(Member member)
    {
        if (supportedRoleIds.contains(ALL_SUPPORTED))
            return true;

        return Optional.ofNullable(member)
                .map(Member::getRoles)
                .map(roles -> roles.stream()
                        .anyMatch(role -> supportedRoleIds.contains(role.getId())))
                .orElse(false);
    }
}
