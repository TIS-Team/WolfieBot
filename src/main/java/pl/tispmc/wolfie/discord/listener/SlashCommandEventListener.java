package pl.tispmc.wolfie.discord.listener;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
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

    @Value("${bot.channels.commands.id:0}")
    private String commandsChannelId;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event)
    {
        if (!event.getChannel().getId().equals(commandsChannelId))
        {
            showWrongChannelMessage(event);
            return;
        }

        for (final SlashCommand slashCommand : this.commandManager.getSlashCommands())
        {
            if (slashCommand.supports(event))
            {
                this.commandManager.processSlashCommand(slashCommand, event);
                break;
            }
        }
    }

    private void showWrongChannelMessage(SlashCommandInteractionEvent event)
    {
        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(":john_wick_angry: Nie ten kanał, zmykaj bo Cię ugryzzzę! :bob_ross_gun:")
                .build();

        event.deferReply()
                .setEphemeral(true)
                .addEmbeds(embed)
                .queue();
    }
}
