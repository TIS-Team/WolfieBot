package pl.tispmc.wolfie.discord.listener;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BotReadyEventListener extends ListenerAdapter
{
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        log.info("Wolfie is ready!");
    }
}
