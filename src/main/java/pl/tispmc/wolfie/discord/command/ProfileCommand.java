package pl.tispmc.wolfie.discord.command;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.Rank;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProfileCommand implements SlashCommand {
    private static final String PLAYER_PARAM = "gracz";
    private final UserDataService userDataService;

    @Override
    public SlashCommandData getSlashCommandData() {
        return SlashCommand.super.getSlashCommandData()
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
        User user = event.getOption(PLAYER_PARAM) != null
                ? event.getOption(PLAYER_PARAM).getAsUser()
                : event.getUser();
        UserData stats = userDataService.find(user.getIdLong());

        if (stats == null) {
            userDataService.save(createNewUserData(user));
            stats = userDataService.find(user.getIdLong());
        }

        Rank rank = calculatePlayerLevel(stats.getExp());
        Rank nextRank = rank.next();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.RED);
        embedBuilder.setThumbnail(user.getEffectiveAvatarUrl());
        embedBuilder.setTitle(" Profil gracza: " + user.getName());
        embedBuilder.addField(":star: `Całkowity` **`EXP`**", String.format("``%s``", stats.getExp()), true);
        embedBuilder.addField(":bar_chart: Ranga", String.format("``%d. %s``", rank.ordinal() + 1, rank.getName()), true);
        embedBuilder.addField(":crossed_swords: Misje", String.format("``%s``", stats.getMissionsPlayed()), true);
        embedBuilder.addField(":medal: Postęp do następnego poziomu", generateProgressBarToNextLevel(rank, stats.getExp()), false);
        if (nextRank.ordinal() > rank.ordinal()) {
            embedBuilder.addField(":small_red_triangle: Następna ranga za:",
                    String.format("``%s EXP do rangi %s``", nextRank.getExp() - stats.getExp(), nextRank.getName()),
                    false);
        } else {
            embedBuilder.addField("\uD83C\uDFC6 Awans niedostępny!", "Osiągnąłeś najwyższą możliwą rangę!", false);
        }
        embedBuilder.addField(":thumbsup: Pochwały", String.format("``%s``", stats.getAppraisalsCount()), true);
        embedBuilder.addField(":thumbsdown: Nagany", String.format("``%s``", stats.getReprimandsCount()), true);
        embedBuilder.addField(":trophy: Nagrody Specjalne", String.format("``%s``", stats.getSpecialAwardCount()), false);
        embedBuilder.setTimestamp(Instant.now());
        event.replyEmbeds(embedBuilder.build()).queue();
    }

    private UserData createNewUserData(User user) {
        return UserData.builder()
                .userId(user.getIdLong())
                .name(user.getName())
                .build();
    }

    private Rank calculatePlayerLevel(int playerExp) {
        return Arrays.stream(Rank.values())
                .filter(rank -> playerExp >= rank.getExp())
                .max(Comparator.comparing(Rank::getExp))
                .orElse(Rank.RECRUIT);
    }

    private String generateProgressBarToNextLevel(Rank rank, int playerExp) {
        int nextLevelRequiredExp = rank.next().getExp();
        int numberOfBars = 10;
        int bar = (int) (((double) playerExp / nextLevelRequiredExp) * 100 / numberOfBars - 1);
        String[] progressBar = new String[numberOfBars];
        for (int i = 0; i < numberOfBars; i++) {
            progressBar[i] = (bar >= i - 1) ? ":green_square:" : ":white_large_square:";
        }
        return String.join("", progressBar);
    }
}
