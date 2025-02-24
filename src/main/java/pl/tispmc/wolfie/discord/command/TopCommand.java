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
        // 1. Pobrać uzytkownikow
        Map<UserId, UserData> userDataMap = userDataService.findAll();
        // 2. Posortowac uzytkownikow po EXP

        List<UserData> sortedUsers = userDataMap.values().stream()
                .sorted(Comparator.comparingInt(UserData::getExp).reversed()) // Sortowanie malejąco
                .limit(10)
                .toList();
        String title = "🏆 Ranking TOP 10 - EXP";

        if (Boolean.TRUE.equals(getBooleanOption(event, LEVEL_PARAM))) {
            sortedUsers = userDataMap.values().stream()
                    .sorted(Comparator.comparingInt(UserData::getLevel).reversed())
                    .limit(10)
                    .toList();
            title = "🏆 Ranking TOP 10 - Poziom";
        } else if (Boolean.TRUE.equals(getBooleanOption(event, MISSIONS_PARAM))) {
            sortedUsers = userDataMap.values().stream()
                    .sorted(Comparator.comparingInt(UserData::getMissionsPlayed).reversed())
                    .limit(10)
                    .toList();
            title = "🏆 Ranking TOP 10 - Misje";
        } else if (Boolean.TRUE.equals(getBooleanOption(event, APPRAISAL_PARAM))) {
            sortedUsers = userDataMap.values().stream()
                    .sorted(Comparator.comparingInt(UserData::getAppraisalsCount).reversed())
                    .limit(10)
                    .toList();
            title = "🏆 Ranking TOP 10 - Pochwały";
        } else if (Boolean.TRUE.equals(getBooleanOption(event, REPRIMAND_PARAM))) {
            sortedUsers = userDataMap.values().stream()
                    .sorted(Comparator.comparingInt(UserData::getReprimandsCount).reversed())
                    .limit(10)
                    .toList();
            title = "🏆 Ranking TOP 10 - Nagany";
        }


        // 3. Zbudowac embed message
        Guild guild = event.getGuild();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.RED);
        if (guild != null) {
            embedBuilder.setThumbnail(guild.getIconUrl());
        }
        embedBuilder.setTimestamp(Instant.now());

        int rank = 1;
        for (UserData user : sortedUsers) {
            String icon = switch (rank) {
                case 1 -> "🏆";
                case 2 -> "🥈";
                case 3 -> "🥉";
                default -> "";
            };
            embedBuilder.addField("#" + rank + " " + icon + " " + user.getName(), "EXP: " + user.getExp(), false);
            rank++;
        }
        // 4. Wyslac embed
        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
