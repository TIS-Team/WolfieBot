package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.mapper.UserMapper;
import pl.tispmc.wolfie.common.model.Evaluation;
import pl.tispmc.wolfie.common.model.User;
import pl.tispmc.wolfie.common.service.UserEvaluationService;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.Optional.ofNullable;

@Component
public class EvaluateCommand extends AbstractSlashCommand
{
    public static final String MM_PARAM = "missionmaker";
    public static final String GM_PARAM = "gamemaster";
    public static final String USER_PARAM  = "gracze";

    private final UserMapper userMapper;
    private final UserEvaluationService userEvaluationService;
    private final String frontEndUrl;

    public EvaluateCommand(
            @Value("${bot.channels.commands.id:0}") String supportedChannelId,
            @Value("${bot.front-end.url}") String frontEndUrl,
            UserEvaluationService userEvaluationService, UserMapper userMapper)
    {
        super(Set.of(supportedChannelId), Set.of(ALL_SUPPORTED));
        this.frontEndUrl = frontEndUrl;
        this.userEvaluationService = userEvaluationService;
        this.userMapper = userMapper;
    }

    @Override
    public SlashCommandData getSlashCommandData()
    {
        return super.getSlashCommandData()
                .addOption(OptionType.STRING, MM_PARAM, "Wybierz Mission Makera", true)
                .addOption(OptionType.STRING, USER_PARAM, "Lista graczy (pingi oddzielone spacją)", true)
                .addOption(OptionType.STRING, GM_PARAM , "Wybierz gamemastera (jeśli jest)");
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
        ReplyCallbackAction replyCallbackAction = event.deferReply();

        if (!hasRequiredRole(event.getMember()))
            throw new CommandException("Brak wymaganej roli do użycia tej komendy.");

        String playersString = event.getOption(USER_PARAM, OptionMapping::getAsString);
        List<User> playerUsers = ofNullable(playersString)
                .map(string -> string.split(" "))
                .map(strings -> Arrays.stream(strings)
                        .map(mention -> mapMentionToUser(event.getGuild(), mention))
                        .toList())
                .orElse(List.of());

        String missionMakerString = event.getOption(MM_PARAM, OptionMapping::getAsString);
        User missionMakerUser = ofNullable(missionMakerString)
                .map(mention -> mapMentionToUser(event.getGuild(), mention))
                .orElse(null);

        String gameMasterString = event.getOption(GM_PARAM, OptionMapping::getAsString);
        List<User> gameMasterUsers = ofNullable(gameMasterString)
                .map(string -> string.split(" "))
                .map(strings -> Arrays.stream(strings)
                        .map(mention -> mapMentionToUser(event.getGuild(), mention))
                        .toList())
                .orElse(List.of());

        Evaluation evaluation = userEvaluationService.generateEvaluation(playerUsers, missionMakerUser, gameMasterUsers);

        replyCallbackAction.setContent(prepareEvaluationUrl(evaluation)).setEphemeral(true).queue();
    }

    private User mapMentionToUser(Guild guild, String mention)
    {
        return ofNullable(mention)
                .map(mentionString -> mentionString.substring(2, mentionString.length() - 1))
                .map(guild::getMemberById)
                .map(Member::getUser)
                .map(this.userMapper::map)
                .orElse(null);
    }

    private String prepareEvaluationUrl(Evaluation evaluation)
    {
        return frontEndUrl + "/evaluation/" + evaluation.getId();
    }
}
