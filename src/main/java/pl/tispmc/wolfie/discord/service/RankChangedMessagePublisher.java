package pl.tispmc.wolfie.discord.service;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.event.model.RankChangedEvent;
import pl.tispmc.wolfie.common.model.Rank;
import pl.tispmc.wolfie.discord.WolfieBot;

import java.awt.*;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RankChangedMessagePublisher
{
    private final WolfieBot wolfieBot;

    @Value("${bot.discord.guild-id}")
    private long guildId;

    @Value("${bot.channels.rank-change.id}")
    private long rankChangeChannelId;

    public void publish(RankChangedEvent.RankChangedEventData data)
    {
        Rank oldRank = data.getOldRank();
        Rank newRank = data.getNewRank();

        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setColor(setupEmbedColor(oldRank, newRank));

        if (oldRank == null || newRank.ordinal() > oldRank.ordinal()) {
            embedBuilder.setColor(Color.GREEN);
        } else {
            embedBuilder.setColor(Color.RED);
        }

        embedBuilder.setTitle("Aktualizacja rangi: " + data.getUsername());
        String oldRankName = Optional.ofNullable(oldRank)
                .map(r -> (r.ordinal() + 1) + ". " + r.getName())
                .orElse("Brak");
        String newRankName = (newRank.ordinal() + 1) + ". " + newRank.getName();
        embedBuilder.addField(":small_red_triangle_down: Poprzednia ranga", oldRankName, true);
        embedBuilder.addField(":small_red_triangle: Nowa ranga", newRankName, true);
        embedBuilder.setThumbnail(data.getAvatarUrl());
        embedBuilder.setTimestamp(Instant.now());

        wolfieBot.getJda()
                .getGuildById(guildId)
                .getTextChannelById(rankChangeChannelId)
                .sendMessageEmbeds(embedBuilder.build())
                .queue();
    }

    private Color setupEmbedColor(Rank oldRank, Rank newRank)
    {
        return Optional.ofNullable(oldRank)
                .map(oldrank -> newRank.ordinal() > oldRank.ordinal() ? Color.GREEN : Color.RED)
                .orElse(Color.RED);
    }
}