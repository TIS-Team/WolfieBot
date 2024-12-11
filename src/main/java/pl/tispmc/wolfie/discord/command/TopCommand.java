package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.util.List;

@Component
public class TopCommand implements SlashCommand
{
    @Override
    public SlashCommandData getSlashCommandData(){
        return SlashCommand.super.getSlashCommandData()
                .addOption(OptionType.STRING, "poziom", "Sortuje ranking po poziomach")
                .addOption(OptionType.STRING, "exp", "Sortuje ranking po EXP")
                .addOption(OptionType.STRING, "misje", "Sortuje ranking po zagranych misjach");
    }

    @Override
    public List<String> getAliases()
    {
        return List.of("top");
    }

    @Override
    public String getDescription()
    {
        return "Pokaż ranking graczy";
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException
    {

    }

}
