package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.command.exception.CommandException;
import pl.tispmc.wolfie.discord.service.UserStatsService;

import java.awt.*;
import java.util.List;

@Component
public class ProfileCommand implements SlashCommand
{
    private static final String PLAYER_PARAM = "gracz";
    private static final String TITLE = "Profil Użytkownika";
    private static final String FIELD_NAME = "Nazwa";
    private static final String FIELD_ID = "ID";
    private static final String FIELD_EXP = "EXP";
    private static final String FIELD_LEVEL = "Poziom";
    private static final String FIELD_MISSIONS = "Misje";
    private static final String FIELD_APPRAISALS = "Pochwały";
    private static final String FIELD_REPRIMANDS = "Nagany";
    private static final String FIELD_SPECIAL_AWARDS = "Nagrody Specjalne";

    private final UserStatsService userStatsService;

    public ProfileCommand(UserStatsService userStatsService) {
        this.userStatsService = userStatsService;
    }

    @Override
    public SlashCommandData getSlashCommandData(){
        return SlashCommand.super.getSlashCommandData()
                .addOption(OptionType.USER, PLAYER_PARAM, "Wybierz gracza");
    }

    @Override
    public List<String> getAliases()
    {
        return List.of("profil");
    }

    @Override
    public String getDescription()
    {
        return "Sprawdź szczegóły swojego profilu bądź wybranego gracza";
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException
    {
        User user = event.getOption(PLAYER_PARAM) != null
                ? event.getOption(PLAYER_PARAM).getAsUser()
                : event.getUser();

        var stats = userStatsService.getUserStats(user.getId());

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(TITLE)
                .addField(FIELD_NAME, user.getName(), false)
                .addField(FIELD_ID, user.getId(), false)
                .addField(FIELD_EXP, String.valueOf(stats.getExp()), false)
                .addField(FIELD_LEVEL, String.valueOf(stats.getLevel()), false)
                .addField(FIELD_MISSIONS, String.valueOf(stats.getMissionsPlayed()), false)
                .addField(FIELD_APPRAISALS, String.valueOf(stats.getAppraisalsCount()), false)
                .addField(FIELD_REPRIMANDS, String.valueOf(stats.getReprimandsCount()), false)
                .addField(FIELD_SPECIAL_AWARDS, String.valueOf(stats.getSpecialAwardCount()), false)
                .setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
