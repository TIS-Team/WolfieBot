package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.util.List;

@Component
public class SummaryCommand implements SlashCommand
{
    @Override
    public SlashCommandData getSlashCommandData()
    {
        return SlashCommand.super.getSlashCommandData()
                .addOption(OptionType.STRING, "gracze", "Lista graczy")
                .addOption(OptionType.STRING, "mission_maker", "Wybierz Mission Makera");
    }

    @Override
    public List<String> getAliases()
    {
        return List.of("ocen");
    }

    @Override
    public String getDescription()
    {
        return "Oceń graczy i gamemastera";
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException
    {

    }
}
