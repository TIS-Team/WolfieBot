package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

@Component
public class ShowChannelAccessCommand extends AbstractSlashCommand
{
    private static final String ROLE_PARAM = "role";

    public ShowChannelAccessCommand(@Value("${bot.roles.admin.id}") String adminRoleId)
    {
        super(Set.of(ALL_SUPPORTED), Set.of(adminRoleId));
    }

    @Override
    public List<String> getAliases()
    {
        return List.of("show_channel_access");
    }

    @Override
    public String getDescription()
    {
        return "Displays what channels a role has access to and what permission it has";
    }

    @Override
    public SlashCommandData getSlashCommandData()
    {
        return super.getSlashCommandData()
                .addOption(OptionType.ROLE, ROLE_PARAM, "Role", true);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException
    {
        ReplyCallbackAction replyCallbackAction = event.deferReply(true);
        Role role = event.getOption(ROLE_PARAM).getAsRole();

        List<GuildChannel> accessibleChannels = event.getGuild().getChannels().stream()
                .filter(role::hasAccess)
                .toList();

        replyCallbackAction.setFiles(prepareFileResponse(role, accessibleChannels)).queue();
//        replyCallbackAction.setContent(prepareResponse(role, accessibleChannels)).queue();
    }

    private FileUpload prepareFileResponse(Role role, List<GuildChannel> accessibleChannels)
    {
        try
        {
            Path tempFilePath = Files.createTempFile("wolfie-role-permissions", null);
            Files.writeString(tempFilePath, prepareResponse(role, accessibleChannels));
            return FileUpload.fromData(new FileInputStream(tempFilePath.toFile()), format("wolfie-role-%s-permissions.txt", role.getName().toUpperCase()));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String prepareResponse(Role role, List<GuildChannel> accessibleChannels)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Uprawnienia roli: ").append(role.getName()).append("\n");

        stringBuilder.append("Dostępne kanały:\n");
        stringBuilder.append("==========================\n");
        for (GuildChannel guildChannel : accessibleChannels)
        {
            stringBuilder.append(guildChannel.getName()).append(format(" (%s)", guildChannel.getId()))
                    .append("\n");
            stringBuilder.append("Uprawnienia: \n");
            for (Permission permission : role.getPermissions(guildChannel))
            {
                stringBuilder.append("- ").append(permission.getName()).append("\n");
            }
            stringBuilder.append("==========================\n");
        }

        //TODO: Create pagination for this and put in embed message.
//        if (stringBuilder.length() > Message.MAX_CONTENT_LENGTH) {
//            stringBuilder.setLength(Message.MAX_CONTENT_LENGTH);
//        }

        return stringBuilder.toString();
    }
}
