package pl.tispmc.wolfie.discord.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.model.UserId;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.discord.WolfieBot;

import java.util.List;
import java.util.Map;

/**
 * A job that fetches users names every hour and updates them in the storage.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserNameUpdateJob
{
    private final WolfieBot wolfieBot;
    private final UserDataService userDataService;

    @Value("${bot.discord.guild-id}")
    private long guildId;

    @Scheduled(cron = "0 0 * * * *")
    public void updateUserNames()
    {
        log.info("Running updateUserNames job");

        Map<UserId, UserData> existingUserDatas = userDataService.findAll();
        List<Member> members = wolfieBot.getJda().getGuildById(guildId).getMembers();

        for (Member member : members)
        {
            UserData userData = existingUserDatas.get(UserId.of(member.getIdLong()));
            if (userData == null)
                continue;

            if (member.getEffectiveName().equals(userData.getName()))
                continue;

            log.info("Updating user name for user id: {}. Old name: {}. New name: {}",
                    userData.getUserId(),
                    userData.getName(),
                    member.getEffectiveName());
            userDataService.save(userData.toBuilder().name(member.getEffectiveName()).build());
        }
    }
}
