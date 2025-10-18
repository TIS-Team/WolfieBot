package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.UserDataCreator;
import pl.tispmc.wolfie.common.event.model.UpdateUserRolesEvent;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.awt.*;
import java.util.List;
import java.util.Set;

@Component
public class ExpCommand extends AbstractSlashCommand
{
    private static final String MEMBER_PARAM = "member";
    private static final String EXP_PARAM = "exp";

    private static final String SUBCOMMAND_ADD = "add";
    private static final String SUBCOMMAND_SET = "set";

    private final UserDataService userDataService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ExpCommand(
            @Value("${bot.channels.commands.id:0}") String supportedChannelId,
            UserDataService userDataService,
            ApplicationEventPublisher applicationEventPublisher)
    {
        super(Set.of(supportedChannelId), Set.of(ALL_SUPPORTED));
        this.userDataService = userDataService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public SlashCommandData getSlashCommandData()
    {
        return super.getSlashCommandData()
                .addSubcommands(
                        new SubcommandData(SUBCOMMAND_ADD, "Dodaj exp graczowi")
                                .addOption(OptionType.USER, MEMBER_PARAM, "Wybierz gracza", true)
                                .addOption(OptionType.INTEGER, EXP_PARAM, "Podaj wartość", true),
                        new SubcommandData(SUBCOMMAND_SET, "Ustaw exp gracza")
                                .addOption(OptionType.USER, MEMBER_PARAM, "Wybierz gracza", true)
                                .addOption(OptionType.INTEGER, EXP_PARAM, "Podaj wartość", true));
    }

    @Override
    public List<String> getAliases()
    {
        return List.of("exp");
    }

    @Override
    public String getDescription()
    {
        return "Add/Subtract given user's exp";
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException
    {
        if (event.getSubcommandName().equals(SUBCOMMAND_ADD))
        {
            handleAddExp(event);
        }
        else if (event.getSubcommandName().equals(SUBCOMMAND_SET))
        {
            handleSetExp(event);
        }
    }

    private void handleAddExp(SlashCommandInteractionEvent event)
    {
        ReplyCallbackAction replyCallbackAction = event.deferReply();
        if (!hasRequiredRole(event.getMember()))
            throw new CommandException("Brak wymaganej roli do użycia tej komendy.");

        Member user = event.getOption(MEMBER_PARAM).getAsMember();
        int exp = event.getOption(EXP_PARAM).getAsInt();

        UserData userData = userDataService.find(user.getIdLong());
        if (userData == null)
        {
            userData = UserDataCreator.createUserData(user);
        }

        UserData updatedUserData = userData.toBuilder().exp(userData.getExp() + exp).build();
        userDataService.save(updatedUserData);

        applicationEventPublisher.publishEvent(new UpdateUserRolesEvent(this, Set.of(user.getIdLong())));
        replyCallbackAction.setEmbeds(new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription("Zaktualizowano exp dla " + user.getEffectiveName())
                .build()).queue();
    }

    private void handleSetExp(SlashCommandInteractionEvent event)
    {
        ReplyCallbackAction replyCallbackAction = event.deferReply();
        if (!hasRequiredRole(event.getMember()))
            throw new CommandException("Brak wymaganej roli do użycia tej komendy.");

        Member user = event.getOption(MEMBER_PARAM).getAsMember();
        int exp = event.getOption(EXP_PARAM).getAsInt();

        UserData userData = userDataService.find(user.getIdLong());
        if (userData == null)
        {
            userData = UserDataCreator.createUserData(user);
        }

        UserData updatedUserData = userData.toBuilder().exp(exp).build();
        userDataService.save(updatedUserData);

        applicationEventPublisher.publishEvent(new UpdateUserRolesEvent(this, Set.of(user.getIdLong())));
        replyCallbackAction.setEmbeds(new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription("Zaktualizowano exp dla " + user.getEffectiveName())
                .build()).queue();
    }
}
