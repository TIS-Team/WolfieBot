package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.UserDataCreator;
import pl.tispmc.wolfie.common.model.Award;
import pl.tispmc.wolfie.common.model.Rank;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.common.util.DateTimeProvider;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class ProfileCommand extends AbstractSlashCommand {
    private static final String PLAYER_PARAM = "gracz";
    private final UserDataService userDataService;
    private final DateTimeProvider dateTimeProvider;

    public ProfileCommand(
            @Value("${bot.channels.commands.id:0}") String supportedChannelId,
            UserDataService userDataService,
            DateTimeProvider dateTimeProvider)
    {
        super(Set.of(supportedChannelId), Set.of(ALL_SUPPORTED));
        this.userDataService = userDataService;
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return super.getSlashCommandData()
                .addOption(OptionType.USER, PLAYER_PARAM, "Wybierz gracza");
    }

    @Override
    public List<String> getAliases() {
        return List.of("profil");
    }

    @Override
    public String getDescription() {
        return "Sprawdź szczegóły swojego profilu bądź wybranego gracza";
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException {
        Member user = event.getOption(PLAYER_PARAM) != null
                ? event.getOption(PLAYER_PARAM).getAsMember()
                : event.getMember();
        UserData stats = userDataService.find(user.getIdLong());

        if (stats == null) {
            userDataService.save(UserDataCreator.createUserData(user));
            stats = userDataService.find(user.getIdLong());
        }

        Rank rank = Rank.getRankForExp(stats.getExp());
        Rank nextRank = rank.next();
        UserData.ExpClaims expClaims = Optional.ofNullable(stats.getExpClaims()).orElse(UserData.ExpClaims.builder().build());
        List<Award> awards = stats.getAwards();

        int dailyExpStreak = expClaims.getDailyExpStreak();
        int dailyExpStreakMaxRecord = expClaims.getDailyExpStreakMaxRecord();
        double expStreakBonus = calculateExpStreakBonus(dailyExpStreak);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.RED);
        embedBuilder.setThumbnail(user.getEffectiveAvatarUrl());
        embedBuilder.setTitle(" Profil gracza: " + user.getEffectiveName());
        embedBuilder.addField(":star: Całkowity EXP", String.format("``%s``", stats.getExp()), true);
        embedBuilder.addField(":bar_chart: Ranga", String.format("``%d. %s``", rank.ordinal() + 1, rank.getName()), true);
        embedBuilder.addField(":crossed_swords: Misje", String.format("``%s``", stats.getMissionsPlayed()), true);

        if (nextRank != null) {
            embedBuilder.addField(":medal: Postęp do następnego poziomu", generateProgressBarToNextLevel(rank, stats.getExp()), false);
            embedBuilder.addField(":small_red_triangle: Następna ranga:",
                    String.format("``%s EXP do rangi %s``", nextRank.getExp() - stats.getExp(), nextRank.getName()),
                    false);
        } else {
            embedBuilder.addField("\uD83C\uDFC6 Awans niedostępny!", "Osiągnąłeś najwyższą możliwą rangę!", false);
        }

        //Nagrody
        embedBuilder.addField(":thumbsup: Pochwały", String.format("``%s``", stats.getAppraisalsCount()), true);
        embedBuilder.addField(":thumbsdown: Nagany", String.format("``%s``", stats.getReprimandsCount()), true);
        embedBuilder.addField("", "", true);

        //Streak
        embedBuilder.addField("\uD83D\uDD25 Daily", String.format("``%d`` dni", dailyExpStreak), true);
        embedBuilder.addField("✨ Bonus do EXP", String.format("``%.0f%%``", expStreakBonus * 100), true);
        embedBuilder.addField("💯 Najdłuższe daily", String.format("``%d`` dni", dailyExpStreakMaxRecord), true);

        if (awards != null && !awards.isEmpty()) {
            StringBuilder awardsContent = new StringBuilder();

            for (Award award : awards) {
                awardsContent.append("")
                        .append(award.getReason())
                        .append(" (")
                        .append(award.getAwardedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                        .append(")\n");
            }

            embedBuilder.addField(":trophy: Wyróżnienia", awardsContent.toString(), false);
        }

        embedBuilder.setTimestamp(dateTimeProvider.currentInstant());
        event.replyEmbeds(embedBuilder.build()).queue();
    }

    private double calculateExpStreakBonus(int dailyExpStreak) {
        int streakBonus = Math.min(dailyExpStreak, 30);
        return ((double) streakBonus / 50);
    }

    private String generateProgressBarToNextLevel(Rank rank, int playerExp) {
        int totalExpForNextRank = rank.next().getExp() - rank.getExp();
        int currentExp = playerExp - rank.getExp();

        int numberOfBars = 10;
        int bar = (int) (((double) currentExp / totalExpForNextRank) * 100 / numberOfBars - 1);
        String[] progressBar = new String[numberOfBars];
        for (int i = 0; i < numberOfBars; i++) {
            progressBar[i] = (bar >= i - 1) ? ":green_square:" : ":white_large_square:";
        }
        return String.join("", progressBar);
    }
}