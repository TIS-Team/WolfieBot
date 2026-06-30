package pl.tispmc.wolfie.discord.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import pl.tispmc.wolfie.common.UserDataCreator;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.discord.config.SilentChannelConfigurationProperties;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class SilentChannelService
{
    private final UserDataService userDataService;
    private final SilentChannelConfigurationProperties silentChannelConfigurationProperties;

    public void handleSilentChannelMessage(GuildMessageChannel channel, Message message, @NonNull Member member)
    {
        if (!hasTargetRoles(member))
        {
            log.info("Silent channel - Member ignored because target roles were not found");
            Message response = channel.sendMessage("Widzę że masz immunitet... ale i tak usuwam Twoją wiadomość... ma tu być cisza...")
                    .setMessageReference(message.getId())
                    .complete();
            response.delete().queueAfter(10, TimeUnit.SECONDS);
            message.delete().queueAfter(10, TimeUnit.SECONDS);
            return;
        }

        String messageContent = message.getContentRaw();
        String attachmentsString = attachmentsToString(message.getAttachments());
        log.info("Silent channel - Banning user: {} {}, because of message: {}, attachments: {}", member.getUser().getName(), member.getEffectiveName(), messageContent, attachmentsString);
        log.info("Silent channel - Deleting 1h timeframe messages for user: {} {}", member.getUser().getName(), member.getEffectiveName());
        try
        {
            String reason = "Pisanie na zakazanym kanale! Kanał: " + channel.getName() + " (" + channel.getId() +  ") | Wiadomość: " + messageContent + " Załączniki: " + attachmentsString;
            channel.sendMessage("Wynocha stąd " + member.getAsMention() + "!").queue();
            member.ban(1, TimeUnit.HOURS).reason(reason).queue();
            saveBanData(member, reason);
        }
        catch (Exception e)
        {
            log.error(MessageFormat.format("Could not ban the given user: name: {0} nickname: {1}", member.getUser().getName(), member.getUser().getEffectiveName()), e);
        }
    }

    private String attachmentsToString(List<Message.Attachment> attachments)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (Message.Attachment attachment : attachments)
        {
            stringBuilder.append("{")
                    .append("filename: ").append(attachment.getFileName())
                    .append(", size: ").append(attachment.getSize())
                    .append(", url: ").append(attachment.getUrl());
        }

        stringBuilder.append("]");

        return stringBuilder.toString();
    }

    private void saveBanData(Member member, String reason)
    {
        UserData userData = Optional.ofNullable(this.userDataService.find(member.getIdLong()))
                .orElse(UserDataCreator.createUserData(member));

        userData.getBans().add(UserData.Ban.builder()
                        .dateTime(LocalDateTime.now())
                        .reason(reason)
                .build());

        this.userDataService.save(userData);
    }

    private boolean hasTargetRoles(Member member)
    {
        List<Role> roles = member.getRoles();
        if (roles.isEmpty() && silentChannelConfigurationProperties.getTarget().isTargetWhenNoRoles())
            return true;

        for (String targetRoleId : silentChannelConfigurationProperties.getTarget().getUserRoleIds())
        {
            if (roles.stream().anyMatch(role -> role.getId().equals(targetRoleId)))
                return true;
        }

        return false;
    }
}
