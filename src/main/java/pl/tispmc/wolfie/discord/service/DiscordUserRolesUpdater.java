package pl.tispmc.wolfie.discord.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.event.model.RankChangedEvent;
import pl.tispmc.wolfie.common.model.Rank;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.model.UserId;
import pl.tispmc.wolfie.common.service.RankService;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.discord.WolfieBot;
import pl.tispmc.wolfie.discord.mapper.DiscordRoleMapper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordUserRolesUpdater
{
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserDataService userDataService;
    private final WolfieBot wolfieBot;
    private final ConcurrentLinkedQueue<Set<Long>> userUpdateQueue = new ConcurrentLinkedQueue<>();
    private final RankService rankService;

    @Value("${bot.discord.guild-id}")
    private long guildId;

    public void scheduleRolesUpdate(Set<Long> userIdsToUpdate)
    {
        this.userUpdateQueue.offer(userIdsToUpdate);
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void updateUserRoles()
    {
        if (!userUpdateQueue.isEmpty())
        {
            try
            {
                handleUserRolesUpdate(userUpdateQueue.poll());
            }
            catch (Exception exception)
            {
                log.error("Could not update user roles", exception);
            }
        }
    }

    private void handleUserRolesUpdate(Set<Long> userIdsToUpdate)
    {
        final Map<UserId, UserData> userDataMap = userDataService.findAll();

        final List<UserData> userDataToUpdate = userDataMap.entrySet().stream()
                .filter(entry -> userIdsToUpdate.contains(entry.getKey().getId()))
                .map(Map.Entry::getValue)
                .toList();

        final Map<UserId, Rank> newRanks = userDataToUpdate.stream()
                .collect(Collectors.toMap(userdata -> UserId.of(userdata.getUserId()), this::calculateUserRank));

        final List<Member> membersToUpdate = this.wolfieBot.getJda().getGuildById(guildId).getMembers().stream()
                .filter(member -> newRanks.containsKey(UserId.of(member.getIdLong())))
                .toList();

        log.info("Updating user roles: {}", newRanks);

        Guild guild = this.wolfieBot.getJda().getGuildById(guildId);
        Map<Long, Rank> supportedRanks = rankService.getSupportedRanks();
        Map<Long, Role> discordRoles = DiscordRoleMapper.map(guild, supportedRanks);

        for (final Member member : membersToUpdate)
        {
            Rank newRank = newRanks.get(UserId.of(member.getIdLong()));
            Role roleToAdd = discordRoles.get(newRank.getId());

            List<Role> currentRoles = guild.getMemberById(member.getIdLong()).getRoles().stream()
                    .filter(role -> discordRoles.containsKey(role.getIdLong()))
                    .toList();

            List<Role> rolesToRemove = member.getRoles().stream()
                    .filter(role -> discordRoles.containsKey(role.getIdLong()))
                    .filter(role -> role.getIdLong() != newRank.getId())
                    .toList();


            if (currentRoles.stream().noneMatch(role -> role.getIdLong() == roleToAdd.getIdLong()))
            {
                guild.modifyMemberRoles(
                        member,
                        List.of(roleToAdd),
                        rolesToRemove
                ).queue();

                publishRankChangedEvent(
                        member.getEffectiveName(),
                        member.getEffectiveAvatarUrl(),
                        rolesToRemove.stream()
                                .findFirst()
                                .map(role -> supportedRanks.get(role.getIdLong()))
                                .orElse(null),
                        newRank);
            }
        }
    }

    private void publishRankChangedEvent(String username, String avatarUrl, Rank oldRank, Rank newRank)
    {
        applicationEventPublisher.publishEvent(new RankChangedEvent(
                this,
                new RankChangedEvent.RankChangedEventData(username, avatarUrl, oldRank, newRank)
        ));
    }

    private Rank calculateUserRank(UserData userData)
    {
        return Arrays.stream(Rank.values())
                .filter(rank -> userData.getExp() >= rank.getExp())
                .max(Comparator.comparing(Rank::getExp))
                .orElseThrow();
    }
}
