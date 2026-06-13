package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.tispmc.wolfie.common.fleetyards.FleetYardsCatalog;
import pl.tispmc.wolfie.common.fleetyards.model.FleetYardsModel;
import pl.tispmc.wolfie.common.model.UserShips;
import pl.tispmc.wolfie.common.service.FleetService;
import pl.tispmc.wolfie.common.util.DateTimeProvider;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HangarCommandTest
{
    private static final Instant NOW_INSTANT = Instant.parse("2026-01-05T12:30:00Z");

    private static final String SUPPORTED_CHANNEL_ID = "123";
    private static final String TIS_VULTURE_ROLE_ID = "456";
    private static final String ADMIN_ROLE_ID = "789";
    private static final long MEMBER_ID = 1;
    private static final long TARGET_MEMBER_ID = 2;
    private static final String MEMBER_NAME = "Brancu";
    private static final String TARGET_MEMBER_NAME = "Nerdi";
    private static final String CUTLASS_SLUG = "drak-cutlass-black";
    private static final String CUTLASS_NAME = "Cutlass Black";

    @Mock
    private FleetService fleetService;
    @Mock
    private FleetYardsCatalog fleetYardsCatalog;
    @Mock
    private DateTimeProvider dateTimeProvider;

    private HangarCommand hangarCommand;

    @Captor
    private ArgumentCaptor<UserShips> userShipsArgumentCaptor;
    @Captor
    private ArgumentCaptor<MessageEmbed> messageEmbedArgumentCaptor;

    @BeforeEach
    void setUp()
    {
        hangarCommand = new HangarCommand(SUPPORTED_CHANNEL_ID, TIS_VULTURE_ROLE_ID, ADMIN_ROLE_ID, fleetService, fleetYardsCatalog, dateTimeProvider);
    }

    @Test
    void shouldReturnCorrectAliases()
    {
        // given
        // when
        // then
        assertThat(hangarCommand.getAliases()).containsExactly("hangar");
    }

    @Test
    void shouldReturnCorrectDescription()
    {
        // given
        // when
        // then
        assertThat(hangarCommand.getDescription()).isEqualTo("Zarządzaj statkami w swoim hangarze");
    }

    @Test
    void shouldContainAllSubcommands()
    {
        // given
        // when
        SlashCommandData slashCommandData = hangarCommand.getSlashCommandData();

        // then
        assertThat(slashCommandData.getSubcommands())
                .extracting(SubcommandData::getName)
                .containsExactly("dodaj", "usun", "wyczysc", "lista");
    }

    @Test
    void shouldSupportOnlyConfiguredChannel()
    {
        // given
        // when
        // then
        assertThat(hangarCommand.supportsChannel(SUPPORTED_CHANNEL_ID)).isTrue();
        assertThat(hangarCommand.supportsChannel("999")).isFalse();
    }

    @Test
    void shouldSupportOnlyConfiguredRole()
    {
        // given
        // when
        // then
        assertThat(hangarCommand.supportsRole(TIS_VULTURE_ROLE_ID)).isTrue();
        assertThat(hangarCommand.supportsRole("999")).isFalse();
    }

    @Test
    void shouldThrowCommandExceptionWhenMemberLacksRequiredRole()
    {
        // given
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);
        Member member = mock(Member.class);

        given(event.getMember()).willReturn(member);
        given(member.getRoles()).willReturn(List.of());

        // when
        Exception exception = catchException(() -> hangarCommand.onSlashCommand(event));

        // then
        assertThat(exception).isInstanceOf(CommandException.class);
        assertThat(exception.getMessage()).isEqualTo("Brak wymaganej roli do użycia tej komendy.");
    }

    @Test
    void shouldThrowCommandExceptionWhenShipAlreadyInHangar()
    {
        // given
        SlashCommandInteractionEvent event = prepareEventWithRequiredRole("dodaj");
        prepareShipOption(event);

        given(fleetYardsCatalog.findBySlug(CUTLASS_SLUG)).willReturn(Optional.of(shipModel()));
        given(fleetService.find(MEMBER_ID)).willReturn(prepareUserShips());

        // when
        Exception exception = catchException(() -> hangarCommand.onSlashCommand(event));

        // then
        assertThat(exception).isInstanceOf(CommandException.class);
        assertThat(exception.getMessage()).isEqualTo("Masz już statek 'Cutlass Black' w swoim hangarze.");
    }

    @Test
    void shouldAddShipToHangar()
    {
        // given
        SlashCommandInteractionEvent event = prepareEventWithRequiredRole("dodaj");
        ReplyCallbackAction replyCallbackAction = mock(ReplyCallbackAction.class);
        prepareShipOption(event);

        given(fleetYardsCatalog.findBySlug(CUTLASS_SLUG)).willReturn(Optional.of(shipModel()));
        given(fleetService.find(MEMBER_ID)).willReturn(null);
        given(event.getMember().getEffectiveName()).willReturn(MEMBER_NAME);
        given(dateTimeProvider.currentInstant()).willReturn(NOW_INSTANT);
        given(event.replyEmbeds(any(MessageEmbed.class))).willReturn(replyCallbackAction);

        // when
        hangarCommand.onSlashCommand(event);

        // then
        verify(fleetService).save(userShipsArgumentCaptor.capture());

        UserShips savedUserShips = userShipsArgumentCaptor.getValue();
        assertThat(savedUserShips.getUserId()).isEqualTo(MEMBER_ID);
        assertThat(savedUserShips.getName()).isEqualTo(MEMBER_NAME);
        assertThat(savedUserShips.getShips()).containsExactly(CUTLASS_SLUG);
    }

    @Test
    void shouldThrowCommandExceptionWhenRemovingShipThatIsNotInHangar()
    {
        // given
        SlashCommandInteractionEvent event = prepareEventWithRequiredRole("usun");
        prepareShipOption(event);

        given(event.getOption("użytkownik")).willReturn(null);
        given(fleetService.find(MEMBER_ID)).willReturn(null);

        // when
        Exception exception = catchException(() -> hangarCommand.onSlashCommand(event));

        // then
        assertThat(exception).isInstanceOf(CommandException.class);
        assertThat(exception.getMessage()).isEqualTo("Nie masz takiego statku w swoim hangarze.");
    }

    @Test
    void shouldThrowCommandExceptionWhenManagingAnotherUserHangarWithoutAdminRole()
    {
        // given
        SlashCommandInteractionEvent event = prepareEventWithRequiredRole("usun");
        User user = mock(User.class);
        User targetUser = mock(User.class);
        OptionMapping userOption = mock(OptionMapping.class);

        given(event.getUser()).willReturn(user);
        given(user.getIdLong()).willReturn(MEMBER_ID);
        given(event.getOption("użytkownik")).willReturn(userOption);
        given(userOption.getAsUser()).willReturn(targetUser);
        given(targetUser.getIdLong()).willReturn(TARGET_MEMBER_ID);

        // when
        Exception exception = catchException(() -> hangarCommand.onSlashCommand(event));

        // then
        assertThat(exception).isInstanceOf(CommandException.class);
        assertThat(exception.getMessage()).isEqualTo("Tylko administrator może zarządzać hangarem innego gracza.");
    }

    @Test
    void shouldClearOwnHangar()
    {
        // given
        SlashCommandInteractionEvent event = prepareEventWithRequiredRole("wyczysc");
        ReplyCallbackAction replyCallbackAction = mock(ReplyCallbackAction.class);
        User user = mock(User.class);

        given(event.getUser()).willReturn(user);
        given(user.getIdLong()).willReturn(MEMBER_ID);
        given(fleetService.find(MEMBER_ID)).willReturn(prepareUserShips());
        given(dateTimeProvider.currentInstant()).willReturn(NOW_INSTANT);
        given(event.replyEmbeds(any(MessageEmbed.class))).willReturn(replyCallbackAction);

        // when
        hangarCommand.onSlashCommand(event);

        // then
        verify(fleetService).save(userShipsArgumentCaptor.capture());

        UserShips savedUserShips = userShipsArgumentCaptor.getValue();
        assertThat(savedUserShips.getName()).isEqualTo(MEMBER_NAME);
        assertThat(savedUserShips.getShips()).isEmpty();
    }

    @Test
    void shouldAllowAdminToClearAnotherUserHangar()
    {
        // given
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);
        Member member = mock(Member.class);
        Role vultureRole = mock(Role.class);
        Role adminRole = mock(Role.class);
        ReplyCallbackAction replyCallbackAction = mock(ReplyCallbackAction.class);
        User user = mock(User.class);
        User targetUser = mock(User.class);
        OptionMapping userOption = mock(OptionMapping.class);

        given(event.getMember()).willReturn(member);
        given(member.getRoles()).willReturn(List.of(vultureRole, adminRole));
        given(vultureRole.getId()).willReturn(TIS_VULTURE_ROLE_ID);
        given(adminRole.getId()).willReturn(ADMIN_ROLE_ID);
        given(event.getSubcommandName()).willReturn("wyczysc");
        given(event.getUser()).willReturn(user);
        given(user.getIdLong()).willReturn(MEMBER_ID);
        given(event.getOption("użytkownik")).willReturn(userOption);
        given(userOption.getAsUser()).willReturn(targetUser);
        given(targetUser.getIdLong()).willReturn(TARGET_MEMBER_ID);
        given(fleetService.find(TARGET_MEMBER_ID)).willReturn(prepareTargetUserShips());
        given(dateTimeProvider.currentInstant()).willReturn(NOW_INSTANT);
        given(event.replyEmbeds(any(MessageEmbed.class))).willReturn(replyCallbackAction);

        // when
        hangarCommand.onSlashCommand(event);

        // then
        verify(fleetService).save(userShipsArgumentCaptor.capture());

        UserShips savedUserShips = userShipsArgumentCaptor.getValue();
        assertThat(savedUserShips.getUserId()).isEqualTo(TARGET_MEMBER_ID);
        assertThat(savedUserShips.getName()).isEqualTo(TARGET_MEMBER_NAME);
        assertThat(savedUserShips.getShips()).isEmpty();
    }

    @Test
    void shouldListShipsInHangar()
    {
        // given
        SlashCommandInteractionEvent event = prepareEventWithRequiredRole("lista");
        ReplyCallbackAction replyCallbackAction = mock(ReplyCallbackAction.class);
        User user = mock(User.class);

        given(event.getOption("użytkownik")).willReturn(null);
        given(event.getUser()).willReturn(user);
        given(user.getIdLong()).willReturn(MEMBER_ID);
        given(user.getEffectiveName()).willReturn(MEMBER_NAME);
        given(user.getEffectiveAvatarUrl()).willReturn("https://cdn.example/avatar.png");
        given(fleetService.find(MEMBER_ID)).willReturn(prepareUserShips());
        given(fleetYardsCatalog.findBySlug(CUTLASS_SLUG)).willReturn(Optional.of(shipModel()));
        given(dateTimeProvider.currentInstant()).willReturn(NOW_INSTANT);
        given(event.replyEmbeds(messageEmbedArgumentCaptor.capture())).willReturn(replyCallbackAction);

        // when
        hangarCommand.onSlashCommand(event);

        // then
        MessageEmbed messageEmbed = messageEmbedArgumentCaptor.getValue();
        assertThat(messageEmbed.getDescription()).contains(CUTLASS_NAME);
    }

    @Test
    void shouldShowEmptyHangarMessageWhenNoShips()
    {
        // given
        SlashCommandInteractionEvent event = prepareEventWithRequiredRole("lista");
        ReplyCallbackAction replyCallbackAction = mock(ReplyCallbackAction.class);
        User user = mock(User.class);

        given(event.getOption("użytkownik")).willReturn(null);
        given(event.getUser()).willReturn(user);
        given(user.getIdLong()).willReturn(MEMBER_ID);
        given(user.getEffectiveName()).willReturn(MEMBER_NAME);
        given(fleetService.find(MEMBER_ID)).willReturn(null);
        given(dateTimeProvider.currentInstant()).willReturn(NOW_INSTANT);
        given(event.replyEmbeds(messageEmbedArgumentCaptor.capture())).willReturn(replyCallbackAction);

        // when
        hangarCommand.onSlashCommand(event);

        // then
        MessageEmbed messageEmbed = messageEmbedArgumentCaptor.getValue();
        assertThat(messageEmbed.getDescription()).isEqualTo("Brak statków w hangarze.");
    }

    private SlashCommandInteractionEvent prepareEventWithRequiredRole(String subcommandName)
    {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);
        Member member = mock(Member.class);
        Role role = mock(Role.class);

        given(event.getMember()).willReturn(member);
        given(member.getRoles()).willReturn(List.of(role));
        given(role.getId()).willReturn(TIS_VULTURE_ROLE_ID);
        given(event.getSubcommandName()).willReturn(subcommandName);
        return event;
    }

    private void prepareShipOption(SlashCommandInteractionEvent event)
    {
        OptionMapping optionMapping = mock(OptionMapping.class);
        User user = mock(User.class);

        given(event.getOption("statek")).willReturn(optionMapping);
        given(optionMapping.getAsString()).willReturn(CUTLASS_SLUG);
        given(event.getUser()).willReturn(user);
        given(user.getIdLong()).willReturn(MEMBER_ID);
    }

    private FleetYardsModel shipModel()
    {
        return new FleetYardsModel(CUTLASS_SLUG, CUTLASS_NAME, null, null, null, null, null, null, null);
    }

    private UserShips prepareUserShips()
    {
        return UserShips.builder()
                .userId(MEMBER_ID)
                .name(MEMBER_NAME)
                .ships(List.of(CUTLASS_SLUG))
                .build();
    }

    private UserShips prepareTargetUserShips()
    {
        return UserShips.builder()
                .userId(TARGET_MEMBER_ID)
                .name(TARGET_MEMBER_NAME)
                .ships(List.of(CUTLASS_SLUG))
                .build();
    }
}
