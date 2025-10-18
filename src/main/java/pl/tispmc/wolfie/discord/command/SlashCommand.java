package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.util.List;

public interface SlashCommand
{
    /**
     * Aliases of the command.
     *
     * Implementations should provide aliases which can be used to execute command.
     * @return the list of command aliases
     */
    List<String> getAliases();

    /**
     * The description of the command. Mostly used in HelpCommand.
     *
     * @return the description of the command.
     */
    String getDescription();

    default SlashCommandData getSlashCommandData()
    {
        return Commands.slash(getAliases().getFirst(), getDescription())
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }

    /**
     * A method responsible for executing command logic.
     *
     * @param event the event to handle
     * @throws CommandException the exception
     */
    void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException;

    /**
     * Interface implementations can implement this method to react to slash command button clicks.
     *
     * @param event the event to handle
     * @throws CommandException the exception
     */
    default void onButtonClick(ButtonInteractionEvent event) throws CommandException {}

    /**
     * Interface implementations can implement this method to react to slash auto complete event.
     *
     * @param event the event to handle
     * @throws CommandException the exception
     */
    default void onAutoComplete(CommandAutoCompleteInteractionEvent event) throws CommandException {}

    /**
     * Determines if implementation of this interface supports the given event.
     *
     * By default, it is determined by checking the slash command alias with {@link SlashCommand#getAliases()}
     *
     * @param event the event to handle
     * @return true if supports, false if not
     */
    default boolean supports(SlashCommandInteractionEvent event)
    {
        return getAliases().contains(event.getName());
    }

    /**
     * Determines if implementation of this interface supports the given button event.
     *
     * By default, returns false, as not every slash command uses buttons.
     *
     * @param event the event to handle
     * @return true if supports, false if not
     */
    default boolean supports(ButtonInteractionEvent event)
    {
        return false;
    }

    /**
     * Determines if the implementation of this interface supports the {@link CommandAutoCompleteInteractionEvent}.
     *
     * By default, it is determined by checking the slash command options
     *
     * @param event the event to handle
     * @return true if supports, false if not
     */
    default boolean supports(CommandAutoCompleteInteractionEvent event)
    {
        SlashCommandData slashCommandData = getSlashCommandData();
        return slashCommandData.getName().equals(event.getName())
                && slashCommandData.getOptions().stream().anyMatch(option -> option.getName().equals(event.getFocusedOption().getName()));
    }

    boolean supportsChannel(String channelId);

    boolean supportsRole(String roleId);
}