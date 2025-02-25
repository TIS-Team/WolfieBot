package pl.tispmc.wolfie.discord.command;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static java.lang.String.format;

@RequiredArgsConstructor
@Component
public class TopCommand implements SlashCommand
{
    private enum RankingBy
    {
        LEVEL("poziom"),
        MISSIONS("misje"),
        APPRAISALS("pochwały"),
        REPRIMANDS("nagany"),
        AWARDS("nagrody"),
        EXP("exp"),
        CURRENT_STREAK("streak"),
        MAX_STREAK("maxstreak");

        private final String paramName;

        RankingBy(String paramName)
        {
            this.paramName = paramName;
        }

        public String getParamName()
        {
            return this.paramName;
        }
    }

    private static final int ENTRIES_PER_PAGE = 10;

    private static final Map<RankingBy, Function<List<UserData>, TopMessageParams>> SORTING_METHODS = Map.of(
            RankingBy.LEVEL, TopCommand::rankByLevel,
            RankingBy.APPRAISALS, TopCommand::rankByAppraisals,
            RankingBy.REPRIMANDS, TopCommand::rankByReprimands,
            RankingBy.MISSIONS, TopCommand::rankByMissions,
            RankingBy.EXP, TopCommand::rankByExp,
            RankingBy.AWARDS, TopCommand::rankByAwards,
            RankingBy.CURRENT_STREAK, TopCommand::rankByCurrentStreak,
            RankingBy.MAX_STREAK, TopCommand::rankByMaxStreak
    );

    private final UserDataService userDataService;

    @Override
    public SlashCommandData getSlashCommandData(){
        return SlashCommand.super.getSlashCommandData()
                .addOption(OptionType.BOOLEAN, RankingBy.EXP.getParamName(), "Sortuje ranking po EXP", false)
                .addOption(OptionType.BOOLEAN, RankingBy.APPRAISALS.getParamName(), "Sortuje ranking po liczbie pochwał", false)
                .addOption(OptionType.BOOLEAN, RankingBy.REPRIMANDS.getParamName(), "Sortuje ranking po liczbie nagan", false)
                .addOption(OptionType.BOOLEAN, RankingBy.MISSIONS.getParamName(), "Sortuje ranking po zagranych misjach", false)
                .addOption(OptionType.BOOLEAN, RankingBy.LEVEL.getParamName(), "Sortuje ranking po poziomach", false)
                .addOption(OptionType.BOOLEAN, RankingBy.AWARDS.getParamName(), "Sortuje ranking po liczbie nagrod", false)
                .addOption(OptionType.BOOLEAN, RankingBy.CURRENT_STREAK.getParamName(), "Sortuje ranking po aktualnym streaku", false)
                .addOption(OptionType.BOOLEAN, RankingBy.MAX_STREAK.getParamName(), "Sortuje ranking po najwyższym streaku", false);
    }


    @Override
    public List<String> getAliases()
    {
        return List.of("top");
    }

    @Override
    public String getDescription()
    {
        return "Pokaż ranking graczy";
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException
    {
        Map<UserId, UserData> userDataMap = userDataService.findAll();
        RankingBy selectedRankingBy = getSelectedRankingBy(event);
        TopMessageParams topMessageParams = SORTING_METHODS.get(selectedRankingBy).apply(userDataMap.values().stream().toList());

        Guild guild = event.getGuild();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(topMessageParams.getTitle())
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .setThumbnail(guild.getIconUrl());

        int rank = 1;
        for (UserData user : topMessageParams.getSortedUsers().stream().limit(ENTRIES_PER_PAGE).toList()) {
            String icon = switch (rank) {
                case 1 -> "🥇";
                case 2 -> "🥈";
                case 3 -> "🥉";
                default -> "";
            };

            int value = switch (selectedRankingBy) {
                case LEVEL -> Rank.getRankForExp(user.getExp()).ordinal();
                case MISSIONS -> user.getMissionsPlayed();
                case APPRAISALS -> user.getAppraisalsCount();
                case REPRIMANDS -> user.getReprimandsCount();
                case AWARDS -> user.getSpecialAwardCount();
                case CURRENT_STREAK -> Optional.ofNullable(user.getExpClaims())
                        .map(UserData.ExpClaims::getDailyExpStreak)
                        .orElse(0);
                case MAX_STREAK -> Optional.ofNullable(user.getExpClaims())
                        .map(UserData.ExpClaims::getDailyExpStreakMaxRecord)
                        .orElse(0);
                default -> user.getExp();
            };

            embedBuilder.addField(
                    format("#%s %s %s", rank, icon, user.getName()),
                    format("%s `%s`", topMessageParams.getValueLabel(), value),
                    false);
            rank++;
        }

        event.replyEmbeds(embedBuilder.build()).queue();
    }

    private static TopMessageParams rankByLevel(List<UserData> userData)
    {
        return TopMessageParams.builder()
                .sortedUsers(userData.stream()
                        .sorted(Comparator.comparingInt((ToIntFunction<UserData>) value -> Rank.getRankForExp(value.getExp()).getExp()).reversed())
                        .toList())
                .title("Ranking TOP 10 - **Poziom** :chart_with_upwards_trend:")
                .valueLabel("Poziom: ")
                .build();
    }

    private static TopMessageParams rankByAppraisals(List<UserData> userData)
    {
        return TopMessageParams.builder()
                .sortedUsers(userData.stream()
                        .sorted(Comparator.comparingInt(UserData::getAppraisalsCount).reversed())
                        .toList())
                .title("Ranking TOP 10 - **Pochwały** :thumbsup:")
                .valueLabel("Pochwały: ")
                .build();
    }

    private static TopMessageParams rankByReprimands(List<UserData> userData)
    {
        return TopMessageParams.builder()
                .sortedUsers(userData.stream()
                        .sorted(Comparator.comparingInt(UserData::getReprimandsCount).reversed())
                        .toList())
                .title("Ranking TOP 10 - **Nagany** :thumbsdown:")
                .valueLabel("Nagany: ")
                .build();
    }

    private static TopMessageParams rankByMissions(List<UserData> userData)
    {
        return TopMessageParams.builder()
                .sortedUsers(userData.stream()
                        .sorted(Comparator.comparingInt(UserData::getMissionsPlayed).reversed())
                        .toList())
                .title("🏆 Ranking TOP 10 - **Misje** \uD83D\uDCE5")
                .valueLabel("Misje: ")
                .build();
    }

    private static TopMessageParams rankByExp(List<UserData> userData)
    {
        return TopMessageParams.builder()
                .sortedUsers(userData.stream()
                        .sorted(Comparator.comparingInt(UserData::getExp).reversed())
                        .toList())
                .title("Ranking TOP 10 - EXP :sparkles:")
                .valueLabel("EXP: ")
                .build();
    }

    private static TopMessageParams rankByAwards(List<UserData> userData)
    {
        return TopMessageParams.builder()
                .sortedUsers(userData.stream()
                        .sorted(Comparator.comparingInt(UserData::getSpecialAwardCount).reversed())
                        .toList())
                .title("Ranking TOP 10 - **Nagrody specjalne** \uD83C\uDFC6")
                .valueLabel("Nagrody: ")
                .build();
    }

    private static TopMessageParams rankByCurrentStreak(List<UserData> userData)
    {
        return TopMessageParams.builder()
                .sortedUsers(userData.stream()
                        .sorted(Comparator.comparingInt((ToIntFunction<UserData>) value ->
                                Optional.ofNullable(value.getExpClaims())
                                        .map(UserData.ExpClaims::getDailyExpStreak)
                                        .orElse(0)).reversed())
                        .toList())
                .title("Ranking TOP 10 - **Aktualny streak** 🔥")
                .valueLabel("Streak: ")
                .build();
    }

    private static TopMessageParams rankByMaxStreak(List<UserData> userData)
    {
        return TopMessageParams.builder()
                .sortedUsers(userData.stream()
                        .sorted(Comparator.comparingInt((ToIntFunction<UserData>) value ->
                                Optional.ofNullable(value.getExpClaims())
                                        .map(UserData.ExpClaims::getDailyExpStreakMaxRecord)
                                        .orElse(0)).reversed())
                        .toList())
                .title("Ranking TOP 10 - **Najdłuższy streak** 💯")
                .valueLabel("Najdłuższy streak: ")
                .build();
    }

    private RankingBy getSelectedRankingBy(SlashCommandInteractionEvent event)
    {
        EnumSet<RankingBy> possibleRankings = EnumSet.allOf(RankingBy.class);
        return possibleRankings.stream().filter(possibleRanking -> Optional.ofNullable(event.getOption(possibleRanking.getParamName()))
                        .map(OptionMapping::getAsBoolean)
                        .orElse(false))
                .findFirst()
                .orElse(RankingBy.EXP);
    }

    @Builder
    @Value
    private static class TopMessageParams
    {
        String title;
        String valueLabel;
        List<UserData> sortedUsers;
    }
}