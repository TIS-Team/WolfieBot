package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.Rank;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.model.UserId;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.common.util.DateTimeProvider;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class RankCommand extends AbstractSlashCommand
{
    private static final String USER_PARAM = "użytkownik";

    private final UserDataService userDataService;
    private final DateTimeProvider dateTimeProvider;

    protected RankCommand(
            @Value("${bot.channels.commands.id:0}") String supportedChannelId,
            UserDataService userDataService,
            DateTimeProvider dateTimeProvider
    )
    {
        super(Set.of(supportedChannelId), Set.of(ALL_SUPPORTED));
        this.userDataService = userDataService;
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public SlashCommandData getSlashCommandData(){
        return super.getSlashCommandData()
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

        Map<UserId, UserData> userDataMap = userDataService.findAll();
        UserData targetUserData = userDataMap.get(UserId.of(targetUser.getIdLong()));

        if (targetUserData == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle("❌ Błąd")
                    .setDescription("Użytkownik " + targetUser.getEffectiveName() + " nie został znaleziony w bazie danych.")
                    .setColor(Color.RED)
                    .setTimestamp(dateTimeProvider.currentInstant());

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


        int totalUsers = userDataMap.size();
        // embed
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("\uD83D\uDCCA Pozycje w rankingach - " + targetUserData.getName())
                .setColor(Color.RED)
                .setTimestamp(dateTimeProvider.currentInstant())
                .setThumbnail(targetUser.getEffectiveAvatarUrl())
                .setDescription("**Użytkownik:** " + targetUserData.getName() + "\n"
                        + "**Liczba osób w rankingu:** `" + totalUsers + "`\n\n"
                        + "---\n"
                        + "\n"
                        + ":star: EXP: **" + expRank + " miejsce** | `" + targetUserData.getExp() + "`\n"
                        + "\n"
                        + "📈 Ranga: **" + rankPosition + " miejsce** | `" + (userRank.ordinal() + 1) + ". " + userRank.getName() + "`\n"
                        + "\n"
                        + "🎯 Misje: **" + missionsRank + " miejsce** | `" + targetUserData.getMissionsPlayed() + "`\n"
                        + "\n"
                        + "👍 Pochwały: **" + appraisalsRank + " miejsce** | `" + targetUserData.getAppraisalsCount() + "`\n"
                        + "\n"
                        + "👎 Nagany: **" + reprimandsRank + " miejsce** | `" + targetUserData.getReprimandsCount() + "`\n"
                        + "\n"
                        + "🏆 Nagrody specjalne: **" + specialAwardsRank + " miejsce** | `" + targetUserData.getSpecialAwardCount() + "`\n"
                        + "\n"
                        + "🔥 Daily: **" + streakRank + " miejsce** | `" + targetUserData.getExpClaims().getDailyExpStreak() + "` dni\n"
                        + "\n"
                        + "💯 Najdłuższe daily: **" + maxStreakRank + " miejsce** | `" + targetUserData.getExpClaims().getDailyExpStreakMaxRecord() + "` dni\n"
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