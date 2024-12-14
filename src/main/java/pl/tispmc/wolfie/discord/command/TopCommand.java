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

    public static final String LEVEL_PARAM = "poziom";
    public static final String EXP_PARAM = "exp";
    public static final String MISSIONS_PARAM = "misje";

    @Override
    public SlashCommandData getSlashCommandData(){
        return SlashCommand.super.getSlashCommandData()
                .addOption(OptionType.BOOLEAN, LEVEL_PARAM, "Sortuje ranking po poziomach")
                .addOption(OptionType.BOOLEAN, EXP_PARAM, "Sortuje ranking po EXP")
                .addOption(OptionType.BOOLEAN, MISSIONS_PARAM, "Sortuje ranking po zagranych misjach");
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
