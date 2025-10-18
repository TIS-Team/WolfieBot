package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.UserDataCreator;
import pl.tispmc.wolfie.common.model.Award;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.common.util.DateTimeProvider;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class AwardCommand extends AbstractSlashCommand {
    private static final String PLAYER_PARAM = "gracz";
    private static final String XP_AMOUNT_PARAM = "ilosc";
    private static final String REASON_PARAM = "powod";
    private static final String DATE_PARAM = "data";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    //move to config later
    private static final String AWARD_CHANNEL_ID = "1344731110407405689";

    private final UserDataService userDataService;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public AwardCommand(
            @Value("${bot.channels.commands.id:0}") String supportedChannelId,
            UserDataService userDataService,
            DateTimeProvider dateTimeProvider)
    {
        super(Set.of(supportedChannelId), Set.of(ALL_SUPPORTED));
        this.userDataService = userDataService;
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return super.getSlashCommandData()
                .addOption(OptionType.USER, PLAYER_PARAM, "Wybierz gracza", true)
                .addOption(OptionType.INTEGER, XP_AMOUNT_PARAM, "Ilość przyznawanego EXP", true)
                .addOption(OptionType.STRING, REASON_PARAM, "Powód przyznania nagrody", true)
                .addOption(OptionType.STRING, DATE_PARAM, "Data przyznania nagrody (YYYY-MM-DD)", false);
    }

    @Override
    public List<String> getAliases() {
        return List.of("nagroda");
    }

    @Override
    public String getDescription() {
        return "Przyznaj graczowi nagrodę wraz z EXP";
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException {
        if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            throw new CommandException("Nie masz uprawnień do używania tej komendy!");
        }

        Member targetMember = event.getOption(PLAYER_PARAM).getAsMember();
        int xpAmount = event.getOption(XP_AMOUNT_PARAM).getAsInt();
        String reason = event.getOption(REASON_PARAM).getAsString();

        ZonedDateTime awardedAt = dateTimeProvider.currentZonedDateTime();
        Instant awardTimestamp = dateTimeProvider.currentInstant();

        if (event.getOption(DATE_PARAM) != null) {
            String dateStr = event.getOption(DATE_PARAM).getAsString();
            try {
                LocalDate customDate = LocalDate.parse(dateStr, DATE_FORMATTER);
                awardedAt = customDate.atStartOfDay(ZoneId.of("Europe/Warsaw"));
            } catch (DateTimeParseException e) {
                throw new CommandException("Nieprawidłowy format daty. Użyj formatu: YYYY-MM-DD");
            }
        }

        UserData userData = Optional.ofNullable(userDataService.find(targetMember.getIdLong()))
                .orElse(UserDataCreator.createUserData(targetMember));

        Award newAward = Award.builder()
                .reason(reason)
                .awardedAt(awardedAt.toLocalDateTime())
                .build();

        List<Award> awards = new ArrayList<>(Optional.ofNullable(userData.getAwards()).orElse(new ArrayList<>()));
        awards.add(newAward);

        UserData updatedUserData = userData.toBuilder()
                .exp(userData.getExp() + xpAmount)
                .awards(awards)
                .specialAwardCount(userData.getSpecialAwardCount() + 1)
                .build();

        userDataService.save(updatedUserData);

        MessageEmbed messageEmbed = new EmbedBuilder()
                .setColor(Color.YELLOW)
                .setTitle(reason)
                .setThumbnail(targetMember.getEffectiveAvatarUrl())
                .setDescription("Przyznano nagrodę specjalną!")
                .addField("Gracz", targetMember.getAsMention(), true)
                .addField("EXP", "+" + xpAmount, true)
                .setTimestamp(awardTimestamp)
                .build();

        event.deferReply().setEphemeral(true).addEmbeds(messageEmbed).queue();

        try {
            TextChannel awardChannel = event.getGuild().getTextChannelById(AWARD_CHANNEL_ID);
            if (awardChannel != null) {
                awardChannel.sendMessageEmbeds(messageEmbed).queue();
            }
        } catch (Exception e) {
            System.err.println("Nie udało się wysłać wiadomości na kanał nagród: " + e.getMessage());
        }
    }
}