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

        String contentDisplayMessage = message.getContentDisplay();
        log.info("Silent channel - Banning user: {} {}, because of message: {}", member.getUser().getName(), member.getEffectiveName(), contentDisplayMessage);
        log.info("Silent channel - Deleting 1h timeframe messages for user: {} {}", member.getUser().getName(), member.getEffectiveName());
        try
        {
            String reason = "Pisanie na zakazanym kanale! Kanał: " + channel.getName() + " (" + channel.getId() +  ") | Wiadomość: " + contentDisplayMessage;
            channel.sendMessage("Wynocha stąd " + member.getAsMention() + "!").queue();
            member.ban(1, TimeUnit.HOURS).reason(reason).queue();
            saveBanData(member, reason);
        }
        catch (Exception e)
        {
            log.error(MessageFormat.format("Could not ban the given user: name: {0} nickname: {1}", member.getUser().getName(), member.getUser().getEffectiveName()), e);
        }
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
