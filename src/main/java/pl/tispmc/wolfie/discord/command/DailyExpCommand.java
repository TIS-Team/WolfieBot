package pl.tispmc.wolfie.discord.command;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Component
public class DailyExpCommand implements SlashCommand
{
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private static final int DAILY_BASE_EXP = 10;

    private final UserDataService userDataService;

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
        UserData userData = userDataService.find(member.getIdLong());
        LocalDateTime lastDailyExpClaimDate = userData.getExpClaims().getLastDailyExpClaim();
        if (lastDailyExpClaimDate != null && lastDailyExpClaimDate.getDayOfYear() == LocalDateTime.now().getDayOfYear())
        {
            throw new CommandException("Dzienny exp już wykorzystany!");
        }

        int dailyExpStreak = userData.getExpClaims().getDailyExpStreak();
        if (lastDailyExpClaimDate == null
                || Math.abs(lastDailyExpClaimDate.getDayOfYear() - LocalDateTime.now().getDayOfYear()) > 1)
        {
            dailyExpStreak = 0;
        }

        dailyExpStreak += 1;

        int dailyExpStreakMaxRecord = userData.getExpClaims().getDailyExpStreak();
        if (dailyExpStreak > dailyExpStreakMaxRecord) {
            dailyExpStreakMaxRecord = dailyExpStreak;
        }

        double expStreakBonus = calculateExpStreakBonus(dailyExpStreak);
        int expReward = calculateDailyExpReward(expStreakBonus);

        UserData updatedUserData = userData.toBuilder()
                .exp(userData.getExp() + expReward)
                .expClaims(userData.getExpClaims().toBuilder()
                        .dailyExpStreak(dailyExpStreak)
                        .dailyExpStreakMaxRecord(dailyExpStreakMaxRecord)
                        .lastDailyExpClaim(LocalDateTime.now())
                        .build())
                .build();

        userDataService.save(updatedUserData);

        MessageEmbed messageEmbed = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Daily - " + member.getEffectiveName())
                .setThumbnail(member.getEffectiveAvatarUrl())
                .setDescription("Zagarnięto daily exp: `+" + expReward + "` \n" +
                        "\n" +
                        "Obecny streak: `" + dailyExpStreak + "`\n" +
                        "Obecny bonus: `" + (expStreakBonus * 100) + "%`")
                .setTimestamp(Instant.now())
                .build();

        event.deferReply().addEmbeds(messageEmbed).queue();
    }

    private double calculateExpStreakBonus(int dailyExpStreak)
    {
        int streakBonus = Math.min(dailyExpStreak, 15);
        return ((double)streakBonus / 100);
    }

    private int calculateDailyExpReward(double expStreakBonus)
    {
        int randomDailyExp = RANDOM.nextInt(11) + DAILY_BASE_EXP;
        return randomDailyExp + (int)Math.ceil(randomDailyExp * expStreakBonus);
    }
}
