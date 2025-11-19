package pl.tispmc.wolfie.discord.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.tispmc.wolfie.discord.listener.BotReadyEventListener;
import pl.tispmc.wolfie.discord.listener.GuildMemberRemoveListener;
import pl.tispmc.wolfie.discord.listener.RoleChangeListener;
import pl.tispmc.wolfie.discord.listener.SlashCommandEventListener;

@Configuration(proxyBeanMethods = false)
public class JdaConfig
{
    @Bean
    public JDA jda(BotConfig botConfig,
                   BotReadyEventListener botReadyEventListener,
                   RoleChangeListener roleChangeListener,
                   SlashCommandEventListener slashCommandEventListener,
                   GuildMemberRemoveListener guildMemberRemoveListener,
                   pl.tispmc.wolfie.discord.listener.WolfieMentionListener wolfieMentionListener)
    {
        return JDABuilder.createDefault(botConfig.getToken())
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.SCHEDULED_EVENTS, GatewayIntent.MESSAGE_CONTENT)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(botReadyEventListener, slashCommandEventListener, roleChangeListener, guildMemberRemoveListener, wolfieMentionListener)
                .setAutoReconnect(true)
                .setActivity(Activity.customStatus("Ocenia graczy TIS'U"))
                .build();
    }
}
