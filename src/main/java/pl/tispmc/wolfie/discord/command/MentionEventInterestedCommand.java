package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Component
public class MentionEventInterestedCommand extends AbstractSlashCommand
{
    private static final String EVENT_PARAM = "event";

    protected MentionEventInterestedCommand(@Value("${bot.roles.admin.id}") String adminRoleId)
    {
        super();
    }

    @Override
    public SlashCommandData getSlashCommandData()
    {
        return super.getSlashCommandData()
                .addOption(OptionType.STRING, EVENT_PARAM, "Wydarzenie dla którego spingować zainteresownych", true, true);
    }

    @Override
    public List<String> getAliases()
    {
        return List.of(
                "ping_wydarzenie",
                "mention_event_interested"
        );
    }

    @Override
    public String getDescription()
    {
        return "Oznacz osoby zainteresowane danym wydarzeniem";
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException
    {
        ReplyCallbackAction replyCallbackAction = event.deferReply(true);
        String eventId = event.getOption(EVENT_PARAM).getAsString();
        ScheduledEvent scheduledEvent = event.getGuild().getScheduledEventById(eventId);
        if (scheduledEvent == null)
        {
            throw new CommandException(format("Wybrany event '%s' nie istnieje", eventId), true);
        }

        String mentions = scheduledEvent.retrieveInterestedMembers().stream()
                .map(IMentionable::getAsMention)
                .collect(Collectors.joining(" "));
        event.getChannel().sendMessage(mentions).queue();

        replyCallbackAction.setContent("Spingowano zainteresowanych");
        replyCallbackAction.complete();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) throws CommandException
    {
        event.replyChoices(getEventNames(event.getGuild())).queue();
    }

    private List<Command.Choice> getEventNames(Guild guild)
    {
        return guild.getScheduledEvents().stream()
                .map(event -> new Command.Choice(event.getName(), event.getId()))
                .toList();
    }
}
