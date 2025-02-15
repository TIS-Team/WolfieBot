package pl.tispmc.wolfie.discord.command;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.event.model.UpdateUserRolesEvent;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.discord.command.exception.CommandException;
import pl.tispmc.wolfie.discord.command.validation.SlashCommandPrerequisites;

import java.awt.*;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class AddExpCommand implements SlashCommand
{
    private static final String MEMBER_PARAM = "member";
    private static final String EXP_PARAM = "exp";

    private static final String SUBCOMMAND_EXP = "add";

    private final SlashCommandPrerequisites prerequisites;
    private final UserDataService userDataService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public SlashCommandData getSlashCommandData()
    {
        return SlashCommand.super.getSlashCommandData()
                .addSubcommands(new SubcommandData(SUBCOMMAND_EXP, "Add exp to user")
                        .addOption(OptionType.USER, MEMBER_PARAM, "Wybierz gracza", true)
                        .addOption(OptionType.INTEGER, EXP_PARAM, "Podaj wartość", true));
    }

    @Override
    public List<String> getAliases()
    {
        return List.of("addexp");
    }

    @Override
    public String getDescription()
    {
        return "Add/Subtract given user's exp";
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException
    {
        if (event.getSubcommandName().equals(SUBCOMMAND_EXP))
        {
            handleAddExp(event);
        }
    }

    private void handleAddExp(SlashCommandInteractionEvent event)
    {
        ReplyCallbackAction replyCallbackAction = event.deferReply();
        if (!prerequisites.hasGameMasterRole(event.getMember()))
            throw new CommandException("Brak wymaganej roli do użycia tej komendy.");

        Member user = event.getOption(MEMBER_PARAM).getAsMember();
        int exp = event.getOption(EXP_PARAM).getAsInt();

        UserData userData = userDataService.find(user.getIdLong());
        if (userData == null)
        {
            userData = createNewUserData(user.getUser());
        }

        UserData updatedUserData = userData.toBuilder().exp(userData.getExp() + exp).build();
        userDataService.save(updatedUserData);

        applicationEventPublisher.publishEvent(new UpdateUserRolesEvent(this, Set.of(user.getIdLong())));
        replyCallbackAction.setEmbeds(new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription("Zaktualizowano exp dla " + user.getEffectiveName())
                .build()).queue();
    }

    private UserData createNewUserData(User user)
    {
        return UserData.builder()
                .userId(user.getIdLong())
                .name(user.getName())
                .build();
    }
}
