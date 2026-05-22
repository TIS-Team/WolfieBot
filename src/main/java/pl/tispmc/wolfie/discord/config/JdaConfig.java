package pl.tispmc.wolfie.discord.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.tispmc.wolfie.discord.listener.BotReadyEventListener;
import pl.tispmc.wolfie.discord.listener.GuildJoinEventListener;
import pl.tispmc.wolfie.discord.listener.GuildMemberRemoveListener;
import pl.tispmc.wolfie.discord.listener.RoleChangeListener;
import pl.tispmc.wolfie.discord.listener.SilentChannelMessageListener;
import pl.tispmc.wolfie.discord.listener.SlashCommandEventListener;
import pl.tispmc.wolfie.discord.listener.WolfieMentionListener;

@Configuration(proxyBeanMethods = false)
public class JdaConfig
{
    @Bean
    public JDA jda(BotConfig botConfig,
                   BotReadyEventListener botReadyEventListener,
                   RoleChangeListener roleChangeListener,
                   SlashCommandEventListener slashCommandEventListener,
                   GuildMemberRemoveListener guildMemberRemoveListener,
                   WolfieMentionListener wolfieMentionListener,
                   GuildJoinEventListener guildJoinEventListener,
                   SilentChannelMessageListener silentChannelMessageListener)
    {
        return JDABuilder.createDefault(botConfig.getToken())
                .enableCache(CacheFlag.SCHEDULED_EVENTS)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.SCHEDULED_EVENTS, GatewayIntent.MESSAGE_CONTENT)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(botReadyEventListener,
                        slashCommandEventListener,
                        roleChangeListener,
                        guildMemberRemoveListener,
                        wolfieMentionListener,
                        guildJoinEventListener,
                        silentChannelMessageListener)
                .setAutoReconnect(true)
                .setActivity(Activity.customStatus("Obserwuje członków TIS..."))
                .build();
    }
}
