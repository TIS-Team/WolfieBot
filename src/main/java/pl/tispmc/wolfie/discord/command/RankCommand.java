package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.util.List;

@Component
public class RankCommand implements SlashCommand
{


    public static final String USER_PARAM = "gracz";


    @Override
    public SlashCommandData getSlashCommandData(){
        return SlashCommand.super.getSlashCommandData()
                .addOption(OptionType.USER, USER_PARAM, "Wybierz gracza");
    }

    @Override
    public List<String> getAliases()
    {
        return List.of("rank");
    }

    @Override
    public String getDescription()
    {
        return "Sprawdź szczegóły swojego rankingu bądź wybranego gracza";
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException
    {
    }
}