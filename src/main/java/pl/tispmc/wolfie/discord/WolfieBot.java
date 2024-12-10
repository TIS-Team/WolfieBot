package pl.tispmc.wolfie.discord;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.command.SlashCommand;

import java.util.List;

@Component
@AllArgsConstructor
@Getter
public class WolfieBot {

    private final JDA jda;
    private final List<SlashCommand> slashCommands;

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady(){
        try {
            this.jda.awaitReady();
            registerSlashCommands();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerSlashCommands() {
        for (Guild guild : this.jda.getGuilds()) {
            List<SlashCommandData> slashCommandDataList = slashCommands.stream()
                    .map(SlashCommand::getSlashCommandData)
                    .toList();
            guild.updateCommands().addCommands(slashCommandDataList).queue();
        }
    }
}


