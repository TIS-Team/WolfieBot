package pl.tispmc.wolfie.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.config.BotConfig;
import pl.tispmc.wolfie.discord.listener.BotReadyEventListener;

@Component

public class WolfieBot {

    private JDA jda;

    private BotConfig botConfig;
    private BotReadyEventListener botReadyEventListener;

    public WolfieBot(BotReadyEventListener botReadyEventListener, BotConfig botConfig){
        this.botReadyEventListener = botReadyEventListener;
        this.botConfig = botConfig;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady(){
        try {
            this.jda = JDABuilder.createDefault(this.botConfig.getToken())
                    .enableIntents(GatewayIntent.GUILD_MEMBERS,GatewayIntent.GUILD_MESSAGES)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .addEventListeners(this.botReadyEventListener)
                    .setAutoReconnect(true)
                    .setActivity(Activity.customStatus("Ocenia graczy TIS'U"))
                    .build()
                    .awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
