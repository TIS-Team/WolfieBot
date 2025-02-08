package pl.tispmc.wolfie.discord.service;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.Action;
import pl.tispmc.wolfie.common.model.EvaluationSummary;
import pl.tispmc.wolfie.common.model.Rank;
import pl.tispmc.wolfie.discord.WolfieBot;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EvaluationSummaryMessagePublisher
{
    private final WolfieBot wolfieBot;

    @Value("${guild-id}")
    private long guildId;

    @Value("${summary-channel-id}")
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
            MessageEmbed embed = buildSummaryMessageForPlayer(player);
            textChannel.sendMessageEmbeds(embed).queue();
        }
    }

    private MessageEmbed buildSummaryMessageForPlayer(EvaluationSummary.SummaryPlayer player)
    {
        Rank rank = calculatePlayerLevel(player.getExp());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.setThumbnail(player.getAvatarUrl());
        embedBuilder.setTitle(":bar_chart: Podsumowanie: " + player.getName());
        embedBuilder.addField(":chart_with_upwards_trend: Zmiana EXP", String.valueOf(player.getExpChange()), true);
        embedBuilder.addField(":bar_chart: Nowy poziom", rank.getName(), true);
        embedBuilder.addField(":crossed_swords: Misje", String.valueOf(player.getMissionsPlayed()), true);
        embedBuilder.addField(":star: Całkowity EXP", String.valueOf(player.getExp()), true);
        embedBuilder.addField(":medal: Postęp do następnego poziomu", generateProgressBarToNextLevel(rank, player.getExp()), false);
        embedBuilder.addField(":thumbsup: Pochwały", buildActionsString(player, true), false);
        embedBuilder.addField(":thumbsdown: Nagany", buildActionsString(player, false), false);
        embedBuilder.addField(":bar_chart: EXP do następnego poziomu", String.valueOf(rank.next().getExp() - player.getExp()), false);
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
                    .append("(")
                    .append(action.getValue())
                    .append(")")
                    .append("\n");
        }
        return actionsWithExp.toString();
    }

    private String generateProgressBarToNextLevel(Rank rank, int playerExp)
    {
        // 10 bars in total
        int nextLevelRequiredExp = rank.next().getExp();

        int numberOfBars = 10;
        int bar = (int)(((double)playerExp / nextLevelRequiredExp) * 100 / numberOfBars - 1);

        String[] progressBar = new String[numberOfBars];
        for (int i = 0; i < numberOfBars; i++)
        {
            if (bar >= i - 1)
            {
                progressBar[i] = ":green_square:";
            }
            else
            {
                progressBar[i] = ":white_large_square:";
            }
        }

        return String.join("", progressBar);
    }

    private Rank calculatePlayerLevel(int playerExp)
    {
        return Arrays.stream(Rank.values())
                .filter(rank -> playerExp >= rank.getExp())
                .max(Comparator.comparing(Rank::getExp))
                .orElse(Rank.RECRUIT);
    }

    private void publishMissionSummary(TextChannel textChannel, EvaluationSummary evaluationSummary)
    {
        List<EvaluationSummary.SummaryPlayer> playersWithAppraisals = evaluationSummary.getPlayers().stream()
                .filter(summaryPlayer -> summaryPlayer.getActions().stream().anyMatch(action -> action.getValue() > 0))
                .toList();
        List<EvaluationSummary.SummaryPlayer> playersWithReprimands = evaluationSummary.getPlayers().stream()
                .filter(summaryPlayer -> summaryPlayer.getActions().stream().anyMatch(action -> action.getValue() < 0))
                .toList();
        int totalAppraisals = (int) playersWithAppraisals.stream().map(player -> player.getActions().stream().filter(action -> action.getValue() > 0).toList())
                .count();
        int totalReprimands = (int) playersWithReprimands.stream().map(player -> player.getActions().stream().filter(action -> action.getValue() < 0).toList())
                .count();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Podsumowanie misji: " + evaluationSummary.getMissionName());
        embedBuilder.addField(":thumbsup: Liczba pochwał", String.valueOf(totalAppraisals), true);
        embedBuilder.addField(":thumbsdown: Liczba nagan", String.valueOf(totalReprimands), true);

        embedBuilder.addField(":star2: Gracze z pochwałami", playersWithAppraisals.stream().map(EvaluationSummary.SummaryPlayer::getName).collect(Collectors.joining(", ")), false);
        embedBuilder.addField(":star2: Gracze z pochwałami", playersWithReprimands.stream().map(EvaluationSummary.SummaryPlayer::getName).collect(Collectors.joining(", ")), false);

        embedBuilder.setTimestamp(Instant.now());

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
