package pl.tispmc.wolfie.discord.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

    public void publish(String username, @Nullable Rank oldRank, Rank newRank)
    {
        String avatarUrl = null;
        User jdaUser = wolfieBot.getJda().getUsersByName(username, true)
                .stream()
                .findFirst()
                .orElse(null);

        if (jdaUser != null) {
            avatarUrl = jdaUser.getEffectiveAvatarUrl();
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (oldRank == null || newRank.ordinal() > oldRank.ordinal()) {
            embedBuilder.setColor(Color.GREEN);
        } else {
            embedBuilder.setColor(Color.RED);
        }

        if (avatarUrl != null) {
            embedBuilder.setThumbnail(avatarUrl);
        }

        embedBuilder.setTitle(":sparkles: Aktualizacja rangi – " + username);
        String oldRankName = Optional.ofNullable(oldRank)
                .map(r -> (r.ordinal() + 1) + ". " + r.getName())
                .orElse("Brak");
        String newRankName = (newRank.ordinal() + 1) + ". " + newRank.getName();
        embedBuilder.addField(":chart_with_downwards_trend: Poprzednia ranga", oldRankName, true);
        embedBuilder.addField(":chart_with_upwards_trend: Nowa ranga", newRankName, true);
        embedBuilder.setTimestamp(Instant.now());

        wolfieBot.getJda()
                .getGuildById(guildId)
                .getTextChannelById(rankChangeChannelId)
                .sendMessageEmbeds(embedBuilder.build())
                .queue();
    }
}