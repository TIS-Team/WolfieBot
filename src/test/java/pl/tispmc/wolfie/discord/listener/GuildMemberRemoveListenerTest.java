package pl.tispmc.wolfie.discord.listener;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.service.UserDataService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class GuildMemberRemoveListenerTest
{
    private static final long MEMBER_ID = 1L;

    @Mock
    private UserDataService userDataService;

    @InjectMocks
    private GuildMemberRemoveListener listener;

    @Test
    void shouldNotApplyExpPenaltyWhenMemberIsNull()
    {
        // given
        // when
        listener.onGuildMemberRemove(mock(GuildMemberRemoveEvent.class));

        // then
        verifyNoInteractions(userDataService);
    }

    @Test
    void shouldNotApplyExpPenaltyWhenMemberIsBot()
    {
        // given
        GuildMemberRemoveEvent event = mock(GuildMemberRemoveEvent.class);
        Member member = mock(Member.class);
        User user = mock(User.class);
        given(user.isBot()).willReturn(true);
        given(member.getUser()).willReturn(user);
        given(event.getMember()).willReturn(member);

        // when
        listener.onGuildMemberRemove(event);

        // then
        verifyNoInteractions(userDataService);
    }


    @Test
    void shouldNotApplyLeaveExpPenaltyWhenMemberIsNotBotAndUserDataIsNull()
    {
        // given
        GuildMemberRemoveEvent event = mock(GuildMemberRemoveEvent.class);
        Member member = mock(Member.class);
        User user = mock(User.class);
        given(user.isBot()).willReturn(false);
        given(member.getUser()).willReturn(user);
        given(member.getIdLong()).willReturn(MEMBER_ID);
        given(event.getMember()).willReturn(member);

        // when
        listener.onGuildMemberRemove(event);

        // then
        verify(userDataService, times(1)).find(MEMBER_ID);
        verify(userDataService, times(0)).save(any(UserData.class));
    }

    @Test
    void shouldApplyLeaveExpPenalty()
    {
        // given
        GuildMemberRemoveEvent event = mock(GuildMemberRemoveEvent.class);
        Member member = mock(Member.class);
        User user = mock(User.class);
        given(user.isBot()).willReturn(false);
        given(member.getIdLong()).willReturn(MEMBER_ID);
        given(member.getUser()).willReturn(user);
        given(event.getMember()).willReturn(member);

        UserData userData = UserData.builder()
                .userId(MEMBER_ID)
                .exp(100)
                .build();
        given(userDataService.find(MEMBER_ID)).willReturn(userData);

        // when
        listener.onGuildMemberRemove(event);

        // then
        verify(userDataService, times(1)).find(MEMBER_ID);
        verify(userDataService, times(1)).save(userData.toBuilder().exp(50).build());
    }
}