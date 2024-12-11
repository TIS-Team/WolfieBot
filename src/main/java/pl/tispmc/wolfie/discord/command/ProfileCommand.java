package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.util.List;

@Component
public class ProfileCommand implements SlashCommand
{
    @Override
    public SlashCommandData getSlashCommandData(){
        return SlashCommand.super.getSlashCommandData()
                .addOption(OptionType.USER, "gracz", "Wybierz gracza", true);
    }

    @Override
    public List<String> getAliases()
    {
        return List.of("profil");
    }

    @Override
    public String getDescription()
    {
        return "Wyświetl profil gracza";
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException
    {
    }
}