package pl.tispmc.wolfie.discord.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import pl.tispmc.wolfie.discord.config.JoinRolesConfigurationProperties;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        classes = {JoinRolesService.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableConfigurationProperties(JoinRolesConfigurationProperties.class)
class JoinRolesServiceTest
{
    @Autowired
    private JoinRolesConfigurationProperties joinRolesConfigurationProperties;

    private JoinRolesService joinRolesService;

    @BeforeEach
    void setUp()
    {
        joinRolesService = new JoinRolesService(joinRolesConfigurationProperties);
    }

    @Test
    void shouldSetJoinRoles()
    {
        // given
        Member member = mock(Member.class);
        Guild guild = mock(Guild.class);
        given(member.getGuild()).willReturn(guild);
        Role role1 = mockRole(123);
        Role role2 = mockRole(456);
        Role role3 = mockRole(999);
        given(guild.getRoles()).willReturn(List.of(role1, role2, role3));

        given(guild.modifyMemberRoles(any(), any(), any())).willReturn(mock(AuditableRestAction.class));

        // when
        joinRolesService.setJoinRoles(member);

        // then
        verify(guild).modifyMemberRoles(member, List.of(role1, role3), List.of());
    }

    private Role mockRole(long roleId)
    {
        Role role = mock(Role.class);
        given(role.getIdLong()).willReturn(roleId);
        return role;
    }
}