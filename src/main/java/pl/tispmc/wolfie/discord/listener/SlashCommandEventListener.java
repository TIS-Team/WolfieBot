package pl.tispmc.wolfie.discord.listener;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.command.CommandManager;
import pl.tispmc.wolfie.discord.command.SlashCommand;

@Component
@AllArgsConstructor
public class SlashCommandEventListener extends ListenerAdapter
{
    private final CommandManager commandManager;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event)
    {
        for (final SlashCommand slashCommand : this.commandManager.getSlashCommands())
        {
            if (slashCommand.supports(event))
            {
                this.commandManager.processSlashCommand(slashCommand, event);
                break;
            }
        }
    }
}
