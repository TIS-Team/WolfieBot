package pl.tispmc.wolfie.discord.command;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.model.UserId;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.awt.*;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class TopCommand implements SlashCommand
{

    private static final String LEVEL_PARAM = "poziom";
    private static final String EXP_PARAM = "exp";
    private static final String MISSIONS_PARAM = "misje";
    private static final String APPRAISAL_PARAM = "pochwały";
    private static final String REPRIMAND_PARAM = "nagany";

    private final UserDataService userDataService;
    private boolean getBooleanOption(SlashCommandInteractionEvent event, String option) {
        OptionMapping optionMapping = event.getOption(option);
        return optionMapping != null && optionMapping.getAsBoolean();
    }
    @Override
    public SlashCommandData getSlashCommandData(){
        return SlashCommand.super.getSlashCommandData()
                .addOption(OptionType.BOOLEAN, EXP_PARAM, "Sortuje ranking po EXP", false)
                .addOption(OptionType.BOOLEAN, APPRAISAL_PARAM, "Sortuje ranking po liczbie pochwał", false)
                .addOption(OptionType.BOOLEAN, REPRIMAND_PARAM, "Sortuje ranking po liczbie nagan", false)
                .addOption(OptionType.BOOLEAN, MISSIONS_PARAM, "Sortuje ranking po zagranych misjach", false)
                .addOption(OptionType.BOOLEAN, LEVEL_PARAM, "Sortuje ranking po poziomach", false);
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
        Map<UserId, UserData> userDataMap = userDataService.findAll();
        List<UserData> sortedUsers;
        String title;
        String valueLabel;
        String type;

        if (Boolean.TRUE.equals(getBooleanOption(event, LEVEL_PARAM))) {
            sortedUsers = userDataMap.values().stream()
                    .sorted(Comparator.comparingInt(UserData::getLevel).reversed())
                    .limit(10)
                    .toList();
            title = "🏆 Ranking TOP 10 - Poziom";
            valueLabel = "Poziom: ";
            type = "level";
        } else if (Boolean.TRUE.equals(getBooleanOption(event, MISSIONS_PARAM))) {
            sortedUsers = userDataMap.values().stream()
                    .sorted(Comparator.comparingInt(UserData::getMissionsPlayed).reversed())
                    .limit(10)
                    .toList();
            title = "🏆 Ranking TOP 10 - Misje";
            valueLabel = "Misje: ";
            type = "missions";
        } else if (Boolean.TRUE.equals(getBooleanOption(event, APPRAISAL_PARAM))) {
            sortedUsers = userDataMap.values().stream()
                    .sorted(Comparator.comparingInt(UserData::getAppraisalsCount).reversed())
                    .limit(10)
                    .toList();
            title = "🏆 Ranking TOP 10 - Pochwały";
            valueLabel = "Pochwały: ";
            type = "appraisals";
        } else if (Boolean.TRUE.equals(getBooleanOption(event, REPRIMAND_PARAM))) {
            sortedUsers = userDataMap.values().stream()
                    .sorted(Comparator.comparingInt(UserData::getReprimandsCount).reversed())
                    .limit(10)
                    .toList();
            title = "🏆 Ranking TOP 10 - Nagany";
            valueLabel = "Nagany: ";
            type = "reprimands";
        } else {
            sortedUsers = userDataMap.values().stream()
                    .sorted(Comparator.comparingInt(UserData::getExp).reversed())
                    .limit(10)
                    .toList();
            title = "🏆 Ranking TOP 10 - EXP";
            valueLabel = "EXP: ";
            type = "exp";
        }

        Guild guild = event.getGuild();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(title)
                .setColor(Color.RED)
                .setTimestamp(Instant.now());

        if (guild != null) {
            embedBuilder.setThumbnail(guild.getIconUrl());
        }
        int rank = 1;
        for (UserData user : sortedUsers) {
            String icon = switch (rank) {
                case 1 -> "🏆";
                case 2 -> "🥈";
                case 3 -> "🥉";
                default -> "";
            };

            int value = switch (type) {
                case "level" -> user.getLevel();
                case "missions" -> user.getMissionsPlayed();
                case "appraisals" -> user.getAppraisalsCount();
                case "reprimands" -> user.getReprimandsCount();
                default -> user.getExp();
            };

            embedBuilder.addField("#" + rank + " " + icon + " " + user.getName(), valueLabel + "**" + value + "**", false);
            rank++;
        }

        event.replyEmbeds(embedBuilder.build()).queue();
    }
}