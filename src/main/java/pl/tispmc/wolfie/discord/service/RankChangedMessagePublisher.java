package pl.tispmc.wolfie.discord.service;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.Rank;
import pl.tispmc.wolfie.discord.WolfieBot;

import java.awt.*;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RankChangedMessagePublisher
{
    private final WolfieBot wolfieBot;

    @Value("${bot.discord.guild-id}")
    private long guildId;

    @Value("${bot.channels.rank-change.id}")
    private long rankChangeChannelId;


    public void publish(String username, Rank oldRank, Rank newRank)
    {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.RED);
        embedBuilder.setTitle("Aktualizacja rangi");
        embedBuilder.addField(":bust_in_silhouette: Gracz", username, true);
        embedBuilder.addField(":small_red_triangle_down: Poprzednia ranga", oldRank.getName(), true);
        embedBuilder.addField(":small_red_triangle: Nowa ranga", newRank.getName(), true);
        embedBuilder.setTimestamp(Instant.now());

        wolfieBot.getJda()
                .getGuildById(guildId)
                .getTextChannelById(rankChangeChannelId)
                .sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
