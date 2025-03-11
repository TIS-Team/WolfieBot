package pl.tispmc.wolfie.discord.command;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.awt.*;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class CommandManager
{
    private final List<SlashCommand> slashCommands;

    public List<SlashCommand> getSlashCommands()
    {
        return this.slashCommands;
    }

    public void processSlashCommand(SlashCommand slashCommand, SlashCommandInteractionEvent event)
    {
        log.info("User '{}:{}' used command '{}'", event.getUser().getId(), event.getUser().getName(), event.getCommandString());
        InteractionHook interactionHook = event.getHook();
        try
        {
            slashCommand.onSlashCommand(event);
            completeEventIfNotAcknowledged(event, false);
        }
        catch (CommandException exception)
        {
            // Normal Command Exception handling here...
            completeEventIfNotAcknowledged(event, exception.isEphemeral());
            handleSlashCommandException(interactionHook, slashCommand, exception);
        }
        catch (Exception exception)
        {
            // General error...
            completeEventIfNotAcknowledged(event, false);
            handleSlashException(interactionHook, slashCommand, exception);
        }
    }

    private void completeEventIfNotAcknowledged(GenericCommandInteractionEvent event, boolean ephemeral)
    {
        try
        {
            if (!event.isAcknowledged())
            {
                event.deferReply(ephemeral).complete();
            }
        }
        catch (Exception exception)
        {
            // ignored.
        }
    }

    private void handleSlashCommandException(InteractionHook interactionHook, SlashCommand command, CommandException exception)
    {
        log.warn("Command '{}' produced a user error: {}", command.getAliases().getFirst(), exception.getMessage(), exception);
        MessageEmbed messageEmbed = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(exception.getMessage())
                .build();
        interactionHook.setEphemeral(exception.isEphemeral()).editOriginalEmbeds(messageEmbed).queue();
    }

    private void handleSlashException(InteractionHook interactionHook, SlashCommand command, Exception exception)
    {
        log.error("Command '{}' produced a technical error: {}", command.getAliases().getFirst(), exception.getMessage(), exception);
        MessageEmbed messageEmbed = new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription("Błąd: " + exception.getMessage() + "\n\n" +
                        "<@272461089541718017> <@361224662912466944> ZAJMIJCIE SIĘ TYM!")
                .build();
        interactionHook.editOriginalEmbeds(messageEmbed).queue();
    }
}
