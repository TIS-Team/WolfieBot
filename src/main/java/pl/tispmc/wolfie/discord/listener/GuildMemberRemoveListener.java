package pl.tispmc.wolfie.discord.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.service.UserDataService;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildMemberRemoveListener extends ListenerAdapter
{
    private final UserDataService userDataService;

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event)
    {
        Member member = event.getMember();
        if (member != null && !member.getUser().isBot())
        {
            applyLeaveExpPenalty(member);
        }
    }

    private void applyLeaveExpPenalty(Member member)
    {
        UserData userData = userDataService.find(member.getIdLong());
        if (userData == null)
            return;

        int newExp = userData.getExp() / 2;
        log.info("Applying leave exp penalty for user {}, new exp: {}", userData.getName(), newExp);
        userDataService.save(userData.toBuilder().exp(newExp).build());
    }
}
