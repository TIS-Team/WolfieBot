package pl.tispmc.wolfie.discord.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.Action;
import pl.tispmc.wolfie.common.model.EvaluationSummary;
import pl.tispmc.wolfie.common.model.Rank;
import pl.tispmc.wolfie.common.util.DateTimeProvider;
import pl.tispmc.wolfie.discord.WolfieBot;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EvaluationSummaryMessagePublisher
{
    private final WolfieBot wolfieBot;
    private final DateTimeProvider dateTimeProvider;

    @Value("${bot.discord.guild-id}")
    private long guildId;

    @Value("${bot.channels.summary.id}")
    private long summaryChannelId;

    public void publish(EvaluationSummary evaluationSummary)
    {
        TextChannel textChannel = this.wolfieBot.getJda().getGuildById(guildId).getTextChannelById(summaryChannelId);

        publishPlayersSummary(textChannel, evaluationSummary.getPlayers());
        publishMissionSummary(textChannel, evaluationSummary);
    }

    private void publishPlayersSummary(TextChannel textChannel, List<EvaluationSummary.SummaryPlayer> players)
    {
        for (EvaluationSummary.SummaryPlayer player : players)
        {
            try
            {
                MessageEmbed embed = buildSummaryMessageForPlayer(player);
                textChannel.sendMessageEmbeds(embed).queue();
            }
            catch (Exception e)
            {
                log.error("Could not publish player summary", e);
            }
        }
    }

    private MessageEmbed buildSummaryMessageForPlayer(EvaluationSummary.SummaryPlayer player)
    {
        Rank rank = Rank.getRankForExp(player.getExp());
        Rank nextRank = rank.next();
        EmbedBuilder embedBuilder = new EmbedBuilder();

        int expChange = player.getExpChange();
        if (expChange > 0) {
            embedBuilder.setColor(Color.GREEN);
        } else if (expChange < 0) {
            embedBuilder.setColor(Color.RED);
        } else {
            embedBuilder.setColor(Color.GRAY);
        }

        embedBuilder.setThumbnail(player.getAvatarUrl());
        embedBuilder.setTitle(":bar_chart: Podsumowanie: " + player.getName());

        embedBuilder.addField(":chart_with_upwards_trend: Zmiana EXP", String.valueOf(player.getExpChange()), true);
        embedBuilder.addField(":bar_chart: Nowy poziom", rank.getName(), true);
        embedBuilder.addField(":crossed_swords: Misje", String.valueOf(player.getMissionsPlayed()), true);
        embedBuilder.addField(":star: Całkowity EXP", String.valueOf(player.getExp()), true);

        if (nextRank != null && nextRank.ordinal() > rank.ordinal()) {
            embedBuilder.addField(":medal: Postęp do następnego poziomu", generateProgressBarToNextLevel(rank, player.getExp()), false);
            embedBuilder.addField(":small_red_triangle: Następna ranga:",
                    String.format("``%s EXP do rangi %s``", nextRank.getExp() - player.getExp(), nextRank.getName()),
                    false);
        } else {
            embedBuilder.addField("\uD83C\uDFC6 Awans niedostępny!", "Osiągnąłeś najwyższą możliwą rangę!", false);
        }

        embedBuilder.addField(":thumbsup: Pochwały", buildActionsString(player, true), false);
        embedBuilder.addField(":thumbsdown: Nagany", buildActionsString(player, false), false);

        return embedBuilder.build();
    }

    private String buildActionsString(EvaluationSummary.SummaryPlayer player, boolean positive)
    {
        List<Action> actions = player.getActions().stream()
                .filter(action -> positive ? action.getValue() > 0 : action.getValue() < 0)
                .toList();

        StringBuilder actionsWithExp = new StringBuilder();
        for (Action action : actions)
        {
            actionsWithExp.append(action.getDisplayName())
                    .append(" ")
                    .append("(");

            if (positive) {
                actionsWithExp.append("+");
            }
            actionsWithExp.append(action.getValue())
                    .append(" EXP")
                    .append(")")
                    .append("\n");
        }
        return actionsWithExp.toString();
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

    private void publishMissionSummary(TextChannel textChannel, EvaluationSummary evaluationSummary)
    {
        try
        {
            List<EvaluationSummary.SummaryPlayer> playersWithAppraisals = evaluationSummary.getPlayers().stream()
                    .filter(summaryPlayer -> summaryPlayer.getActions().stream().anyMatch(action -> action.getValue() > 0))
                    .toList();
            List<EvaluationSummary.SummaryPlayer> playersWithReprimands = evaluationSummary.getPlayers().stream()
                    .filter(summaryPlayer -> summaryPlayer.getActions().stream().anyMatch(action -> action.getValue() < 0))
                    .toList();
            int totalAppraisals = (int) playersWithAppraisals.stream().flatMap(player -> player.getActions().stream().filter(action -> action.getValue() > 0))
                    .count();
            int totalReprimands = (int) playersWithReprimands.stream().flatMap(player -> player.getActions().stream().filter(action -> action.getValue() < 0))
                    .count();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Podsumowanie misji: " + evaluationSummary.getMissionName());
            embedBuilder.addField(":thumbsup: Liczba pochwał", String.valueOf(totalAppraisals), true);
            embedBuilder.addField(":thumbsdown: Liczba nagan", String.valueOf(totalReprimands), true);

            embedBuilder.addField(":star2: Gracze z pochwałami", playersWithAppraisals.stream().map(EvaluationSummary.SummaryPlayer::getName).collect(Collectors.joining(", ")), false);
            embedBuilder.addField(":warning: Gracze z naganami", playersWithReprimands.stream().map(EvaluationSummary.SummaryPlayer::getName).collect(Collectors.joining(", ")), false);

            embedBuilder.setTimestamp(dateTimeProvider.currentInstant());

            textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
        catch (Exception e)
        {
            log.error("Could not publish mission summary");
        }
    }
}
