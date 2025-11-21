package pl.tispmc.wolfie.discord.command;


import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.command.exception.CommandException;
import pl.tispmc.wolfie.discord.ai.WolfiePersonalityService;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class SelectAiPersonalityCommand extends AbstractSlashCommand
{
    public static final String PERSONALITY_PARAM  = "personality";

    private final WolfiePersonalityService wolfiePersonalityService;

    public SelectAiPersonalityCommand(WolfiePersonalityService wolfiePersonalityService,
                                      @Value("${bot.roles.admin.id}") String adminRoleId)
    {
        super(Set.of(ALL_SUPPORTED), Set.of(adminRoleId));
        this.wolfiePersonalityService = wolfiePersonalityService;
    }

    @Override
    public List<String> getAliases()
    {
        return List.of("zmien_osobowosc", "switch_personality");
    }

    @Override
    public String getDescription()
    {
        return "Pozwala zmienić osobowość Wolfiego (podczas korzystania z AI)";
    }

    @Override
    public SlashCommandData getSlashCommandData()
    {
        return super.getSlashCommandData()
                .addOption(OptionType.STRING, PERSONALITY_PARAM, "Wybierz osobowość Wolfiego", true, true);
    }


    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException
    {
        if(!hasRequiredRole(event.getMember()))
            throw new CommandException("Brak wymaganej roli do użycia tej komendy.");

        ReplyCallbackAction replyCallbackAction = event.deferReply(true);
        String personality =  event.getOption(PERSONALITY_PARAM).getAsString();

        this.wolfiePersonalityService.setPersonality(personality);
        replyCallbackAction.setContent("Osobowość zmieniona na: " + personality);
        replyCallbackAction.complete();
        log.info("{} set Wolfie's personality to: {}", event.getMember().getEffectiveName(), personality);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) throws CommandException
    {
        if (event.getFocusedOption().getName().equals(PERSONALITY_PARAM))
        {
            event.replyChoices(this.wolfiePersonalityService.getAvailablePersonalitiesNames().stream()
                    .map(name -> new Command.Choice(name, name))
                    .toList()).queue();
        }
    }
}
