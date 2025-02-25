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
        InteractionHook interactionHook = event.getHook();
        try
        {
            slashCommand.onSlashCommand(event);
            completeEventIfNotAcknowledged(event);
        }
        catch (CommandException exception)
        {
            // Normal Command Exception handling here...
            completeEventIfNotAcknowledged(event);
            handleSlashCommandException(interactionHook, slashCommand, exception);
        }
        catch (Exception exception)
        {
            // General error...
            completeEventIfNotAcknowledged(event);
            handleSlashException(interactionHook, slashCommand, exception);
        }
    }

    private void completeEventIfNotAcknowledged(GenericCommandInteractionEvent event)
    {
        try
        {
            if (!event.isAcknowledged())
            {
                event.deferReply().complete();
            }
        }
        catch (Exception exception)
        {
            // ignored.
        }
    }

    private void handleSlashCommandException(InteractionHook interactionHook, SlashCommand command, CommandException exception)
    {
        log.error("Błąd: {}", command.getAliases().get(0), exception);
//        log.error(messageSource.getMessage(ERROR_COMMAND_EXCEPTION, command.getAliases().get(0), exception.getMessage()));
        MessageEmbed messageEmbed = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(exception.getMessage())
//                .setDescription(messageSource.getMessage(ERROR_COMMAND_EXCEPTION, exception.getMessage()))
                .build();
        interactionHook.editOriginalEmbeds(messageEmbed).queue();
    }

    private void handleSlashException(InteractionHook interactionHook, SlashCommand command, Exception exception)
    {
        log.error("Błąd: {}", command.getAliases().get(0), exception);
//        log.error(messageSource.getMessage(ERROR_GENERAL, command.getAliases().get(0), exception.getMessage()), exception);
        MessageEmbed messageEmbed = new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription("Błąd: " + exception.getMessage())
                .setFooter("<@272461089541718017> <@361224662912466944> ZAJMIJCIE SIĘ TYM!")
//                .setDescription(messageSource.getMessage(ERROR_GENERAL, exception.getMessage()))
                .build();
        interactionHook.editOriginalEmbeds(messageEmbed).queue();
    }
}
