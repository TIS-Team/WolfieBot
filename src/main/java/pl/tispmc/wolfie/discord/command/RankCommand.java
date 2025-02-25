package pl.tispmc.wolfie.discord.command;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.Rank;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.model.UserId;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class RankCommand implements SlashCommand
{
    private static final String USER_PARAM = "użytkownik";

    private final UserDataService userDataService;

    @Override
    public SlashCommandData getSlashCommandData(){
        return SlashCommand.super.getSlashCommandData()
                .addOption(OptionType.USER, USER_PARAM, "Użytkownik dla którego sprawdzić rankingi (domyślnie ty)", false);
    }

    @Override
    public List<String> getAliases()
    {
        return List.of("rank");
    }

    @Override
    public String getDescription()
    {
        return "Pokaż swoją pozycję w rankingach";
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException
    {
        OptionMapping userOption = event.getOption(USER_PARAM);
        User targetUser = userOption != null ? userOption.getAsUser() : event.getUser();
        String targetUserId = targetUser.getId();

        // Get all user data
        Map<UserId, UserData> userDataMap = userDataService.findAll();
        int totalUsers = userDataMap.size();


        //To do sprawdzenia
        UserData targetUserData = null;
        for (Map.Entry<UserId, UserData> entry : userDataMap.entrySet()) {
            if (entry.getKey().toString().equals(targetUserId) ||
                    (entry.getValue().getName() != null && entry.getValue().getName().equals(targetUser.getName()))) {
                targetUserData = entry.getValue();
                break;
            }
        }

        if (targetUserData == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle("❌ Błąd")
                    .setDescription("Użytkownik " + targetUser.getName() + " nie został znaleziony w bazie danych.")
                    .setColor(Color.RED)
                    .setTimestamp(Instant.now());

            event.replyEmbeds(errorEmbed.build()).queue();
            return;
        }

        int expRank = calculateRank(userDataMap.values(), targetUserData, UserData::getExp);
        Rank userRank = Rank.getRankForExp(targetUserData.getExp());
        int rankPosition = calculateRank(userDataMap.values(), targetUserData, UserData::getExp);
        int missionsRank = calculateRank(userDataMap.values(), targetUserData, UserData::getMissionsPlayed);
        int appraisalsRank = calculateRank(userDataMap.values(), targetUserData, UserData::getAppraisalsCount);
        int reprimandsRank = calculateRank(userDataMap.values(), targetUserData, UserData::getReprimandsCount);
        int specialAwardsRank = calculateRank(userDataMap.values(), targetUserData, UserData::getSpecialAwardCount);
        int streakRank = calculateRank(userDataMap.values(), targetUserData, data -> data.getExpClaims().getDailyExpStreak());
        int maxStreakRank = calculateRank(userDataMap.values(), targetUserData, data -> data.getExpClaims().getDailyExpStreakMaxRecord());


        // embed
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("\uD83D\uDCCA Pozycje w rankingach - " + targetUserData.getName())
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .setThumbnail(targetUser.getEffectiveAvatarUrl())
                .setDescription("**Użytkownik:** " + targetUserData.getName() + "\n"
                        + "**Liczba osób w rankingu:** `" + totalUsers + "`\n\n"
                        + "---\n"
                        + "\n"
                        + ":star: EXP: **" + expRank + " miejsce** | `" + targetUserData.getExp() + "`\n"
                        + "\n"
                        + "📈 Poziom: **" + rankPosition + " miejsce** | `" + (userRank.ordinal() + 1) + ". " + userRank.getName() + "`\n"
                        + "\n"
                        + "🎯 Misje: **" + missionsRank + " miejsce** | `" + targetUserData.getMissionsPlayed() + "`\n"
                        + "\n"
                        + "👍 Pochwały: **" + appraisalsRank + " miejsce** | `" + targetUserData.getAppraisalsCount() + "`\n"
                        + "\n"
                        + "👎 Nagany: **" + reprimandsRank + " miejsce** | `" + targetUserData.getReprimandsCount() + "`\n"
                        + "\n"
                        + "🏆 Nagrody specjalne: **" + specialAwardsRank + " miejsce** | `" + targetUserData.getSpecialAwardCount() + "`\n"
                        + "\n"
                        + "🔥 Streak: **" + streakRank + " miejsce** | `" + targetUserData.getExpClaims().getDailyExpStreak() + "` dni\n"
                        + "\n"
                        + "💯 Najdłuższy streak: **" + maxStreakRank + " miejsce** | `" + targetUserData.getExpClaims().getDailyExpStreakMaxRecord() + "` dni\n"
                        + "\n"
                );


        event.replyEmbeds(embedBuilder.build()).queue();
    }

    // do sprawdzenia
    private <T extends Comparable<T>> int calculateRank(Iterable<UserData> allUsers, UserData targetUser,
                                                        java.util.function.Function<UserData, T> valueExtractor) {
        T targetValue = valueExtractor.apply(targetUser);
        int rank = 1;

        for (UserData user : allUsers) {
            T currentValue = valueExtractor.apply(user);
            if (currentValue.compareTo(targetValue) > 0) {
                rank++;
            }
        }

        return rank;
    }

}