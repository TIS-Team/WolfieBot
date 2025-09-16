package pl.tispmc.wolfie.discord.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.UserDataCreator;
import pl.tispmc.wolfie.common.event.model.UpdateUserRolesEvent;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.common.util.DateTimeProvider;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.awt.*;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Component
public class DailyExpCommand implements SlashCommand
{
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private static final int DAILY_BASE_EXP = 10;

    private final UserDataService userDataService;
    private final DateTimeProvider dateTimeProvider;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public List<String> getAliases()
    {
        return List.of("daily");
    }

    @Override
    public String getDescription()
    {
        return "Zgarnij dzienną porcję darmowego expa";
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException
    {
        Member member = event.getMember();
        UserData userData = Optional.ofNullable(userDataService.find(member.getIdLong())).orElse(UserDataCreator.createUserData(member));
        UserData.ExpClaims expClaims = Optional.ofNullable(userData.getExpClaims()).orElse(UserData.ExpClaims.builder().build());

        ZonedDateTime now = dateTimeProvider.currentZonedDateTime();
        ZonedDateTime lastDailyExpClaimDate = Optional.ofNullable(expClaims.getLastDailyExpClaim()).map(dateTimeProvider::withCorrectZone).orElse(null);
        if (lastDailyExpClaimDate != null && !lastDailyExpClaimDate.toLocalDate().isBefore(now.toLocalDate()))
        {
            throw new CommandException("Dzienny exp już wykorzystany!", true);
        }

        ReplyCallbackAction replyCallbackAction = event.deferReply();
        handleDailyExp(replyCallbackAction, member, userData, expClaims, lastDailyExpClaimDate, now);
        applicationEventPublisher.publishEvent(new UpdateUserRolesEvent(this, Set.of(member.getIdLong())));
    }

    private void handleDailyExp(ReplyCallbackAction replyCallbackAction,
                                Member member,
                                UserData userData,
                                UserData.ExpClaims expClaims,
                                ZonedDateTime lastDailyExpClaimDate,
                                ZonedDateTime now)
    {
        int dailyExpStreak = expClaims.getDailyExpStreak();
        if (lastDailyExpClaimDate == null ||
                !lastDailyExpClaimDate.toLocalDate().plusDays(1).isEqual(now.toLocalDate()) &&
                        !lastDailyExpClaimDate.toLocalDate().isEqual(now.toLocalDate()))
        {
            dailyExpStreak = 0;
        }

        dailyExpStreak += 1;

        int dailyExpStreakMaxRecord = expClaims.getDailyExpStreakMaxRecord();
        if (dailyExpStreak > dailyExpStreakMaxRecord) {
            dailyExpStreakMaxRecord = dailyExpStreak;
        }

        double expStreakBonus = calculateExpStreakBonus(dailyExpStreak);
        int expReward = calculateDailyExpReward(expStreakBonus);

        log.info("Giving daily exp reward {}, for user {} {}", expReward, userData.getName(), userData.getUserId());

        updateUserData(userData, expClaims, expReward, dailyExpStreak, dailyExpStreakMaxRecord, now);
        sendEmbedMessage(replyCallbackAction, member, expReward, dailyExpStreak, expStreakBonus, dailyExpStreakMaxRecord, now);
    }

    private void sendEmbedMessage(ReplyCallbackAction replyCallbackAction,
                                  Member member,
                                  int expReward,
                                  int dailyExpStreak,
                                  double expStreakBonus,
                                  int dailyExpStreakMaxRecord,
                                  ZonedDateTime now)
    {
        MessageEmbed messageEmbed = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Daily - " + member.getEffectiveName())
                .setThumbnail(member.getEffectiveAvatarUrl())
                .setDescription("Zagarnięto daily exp: `+" + expReward + "` \n" +
                        "\n" +
                        "🔥 Daily: `" + dailyExpStreak + "`\n" +
                        ":sparkles: Bonus EXP: `" + String.format("%.0f", expStreakBonus * 100) + "%` \n" +
                        "💯 Najdłuższe daily: `" + dailyExpStreakMaxRecord + "`")
                .setTimestamp(now)
                .build();

        replyCallbackAction.addEmbeds(messageEmbed).queue();
    }

    private void updateUserData(UserData userData,
                                UserData.ExpClaims expClaims,
                                int expReward,
                                int dailyExpStreak,
                                int dailyExpStreakMaxRecord,
                                ZonedDateTime now)
    {
        UserData updatedUserData = userData.toBuilder()
                .exp(userData.getExp() + expReward)
                .expClaims(expClaims.toBuilder()
                        .dailyExpStreak(dailyExpStreak)
                        .dailyExpStreakMaxRecord(dailyExpStreakMaxRecord)
                        .lastDailyExpClaim(now.toLocalDateTime())
                        .build())
                .build();

        userDataService.save(updatedUserData);
    }

    private double calculateExpStreakBonus(int dailyExpStreak)
    {
        int streakBonus = Math.min(dailyExpStreak, 30);
        return ((double)streakBonus / 50);
    }

    private int calculateDailyExpReward(double expStreakBonus)
    {
        int randomDailyExp = RANDOM.nextInt(11) + DAILY_BASE_EXP;
        return randomDailyExp + (int)Math.ceil(randomDailyExp * expStreakBonus);
    }
}