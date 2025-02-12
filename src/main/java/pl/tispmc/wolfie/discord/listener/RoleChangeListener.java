package pl.tispmc.wolfie.discord.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.Rank;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.service.RankService;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.discord.mapper.DiscordRoleMapper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoleChangeListener extends ListenerAdapter
{
    private final RankService rankService;
    private final UserDataService userDataService;

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event)
    {
        Map<Long, Rank> supportedRanks = rankService.getSupportedRanks();

        Rank highestRank = event.getRoles().stream()
                .map(role -> toRank(supportedRanks, role))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(Rank::getExp))
                .orElse(null);

        if (highestRank == null)
            return;

        if (isAddedByBot(event))
            return;

        List<AuditLogEntry> logEntryList = event.getGuild().retrieveAuditLogs()
                .type(ActionType.MEMBER_ROLE_UPDATE)
                .limit(1)
                .complete();

        AuditLogEntry logEntry = logEntryList.isEmpty() ? null : logEntryList.getFirst();

        log.info("User {} added role {} to {}",
                Optional.ofNullable(logEntry).map(AuditLogEntry::getUser).map(User::getName).orElse("Unknown"),
                highestRank,
                event.getUser().getName()
        );

        handleRankChange(event.getGuild(), event.getMember(), highestRank);
    }

    private Rank toRank(Map<Long, Rank> supportedRanks, Role role)
    {
        return supportedRanks.getOrDefault(role.getIdLong(), null);
    }

    private boolean isAddedByBot(GuildMemberRoleAddEvent event)
    {
        return event.getMember().getUser().isBot();
    }

    private void handleRankChange(Guild guild, Member member, Rank rank)
    {
        UserData userData = Optional.ofNullable(userDataService.find(member.getIdLong()))
                        .orElse(createNewUserData(member));
        userData = userData.toBuilder().exp(rank.getExp()).build();
        userDataService.save(userData);

        Map<Long, Role> discordRoles = DiscordRoleMapper.map(guild, Arrays.stream(Rank.values()).toList());

        List<Role> rolesToRemove = discordRoles.values().stream().filter(role -> role.getIdLong() != rank.getId()).toList();

        Role roleToAdd = discordRoles.get(rank.getId());

        guild.modifyMemberRoles(
                member,
                List.of(roleToAdd),
                rolesToRemove
        ).queue();
    }

    private UserData createNewUserData(Member user)
    {
        return UserData.builder()
                .userId(user.getIdLong())
                .name(user.getUser().getName())
                .build();
    }
}
