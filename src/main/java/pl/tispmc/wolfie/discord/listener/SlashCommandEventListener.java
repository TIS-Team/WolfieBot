package pl.tispmc.wolfie.discord.listener;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.command.CommandManager;
import pl.tispmc.wolfie.discord.command.SlashCommand;

import java.awt.*;

@Component
@RequiredArgsConstructor
public class SlashCommandEventListener extends ListenerAdapter
{
    private final CommandManager commandManager;

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event)
    {
        for (final SlashCommand slashCommand : commandManager.getSlashCommands())
        {
            if (slashCommand.supports(event))
            {
                slashCommand.onAutoComplete(event);
                break;
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event)
    {
        for (final SlashCommand slashCommand : this.commandManager.getSlashCommands())
        {
            if (slashCommand.supports(event))
            {
                if (!slashCommand.supportsChannel(event.getChannelId()))
                {
                    showWrongChannelMessage(event);
                    break;
                }
                this.commandManager.processSlashCommand(slashCommand, event);
                break;
            }
        }
    }

    private void showWrongChannelMessage(SlashCommandInteractionEvent event)
    {
        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("<:john_wick_angry:1283761008862822431> Nie ten kanał, zmykaj bo Cię ugryzzzę! <:bob_ross_gun:1326597380996468847>")
                .build();

        event.deferReply()
                .setEphemeral(true)
                .addEmbeds(embed)
                .queue();
    }
}
