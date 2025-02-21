package pl.tispmc.wolfie.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pl.tispmc.wolfie.common.dto.SubmittedUser;
import pl.tispmc.wolfie.common.event.model.EvaluationSummaryEvent;
import pl.tispmc.wolfie.common.event.model.UpdateUserRolesEvent;
import pl.tispmc.wolfie.common.exception.EvaluationNotFoundException;
import pl.tispmc.wolfie.common.mapper.EvaluationUserMapper;
import pl.tispmc.wolfie.common.model.Action;
import pl.tispmc.wolfie.common.model.EvaluationSubmission;
import pl.tispmc.wolfie.common.model.EvaluationSummary;
import pl.tispmc.wolfie.common.model.User;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.model.UserId;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserEvaluationServiceTest
{
    private static final String MISSION_NAME = "mission_name";

    private static final long USER_ID_1 = 1;
    private static final long USER_ID_2 = 2;
    private static final long USER_ID_3 = 3;

    private static final int USER_1_EXP = 50;
    private static final int USER_2_EXP = 100;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private UserDataService userDataService;
    @Mock
    private EvaluationUserMapper evaluationUserMapper;

    @InjectMocks
    private UserEvaluationService userEvaluationService;

    @Captor
    private ArgumentCaptor<EvaluationSummaryEvent> evaluationSummaryEventArgumentCaptor;
    @Captor
    private ArgumentCaptor<UpdateUserRolesEvent> updateUserRolesEventArgumentCaptor;
    @Captor
    private ArgumentCaptor<Collection<UserData>> expectedUserDataArgumentCaptor;

    @Test
    void shouldThrowEvaluationNotfoundExceptionWhenNoEvaluationForGivenId()
    {
        // given
        // when
        // then
        assertThrows(EvaluationNotFoundException.class, () -> userEvaluationService.submitEvaluation(UUID.randomUUID(), new EvaluationSubmission()));
    }

    @Test
    void shouldSubmitEvaluation()
    {
        // given
        UUID evaluationId = userEvaluationService.generateEvaluation(
                List.of(User.builder().id(USER_ID_1).build(), User.builder().id(USER_ID_3).build()),
                User.builder().id(USER_ID_2).build(),
                List.of())
                .getId();
        EvaluationSubmission submission = new EvaluationSubmission();
        submission.setMissionName(MISSION_NAME);
        submission.setUsers(prepareSubmittedUsers(
                prepareSubmittedUser(USER_ID_1, EnumSet.of(Action.MAIN_OBJECTIVE_COMPLETED)),
                prepareSubmittedUser(USER_ID_3, EnumSet.of(Action.SPECTACULAR_ACHIEVEMENTS_IN_FIGHT))
        ));

        given(userDataService.findAll()).willReturn(prepareExistingUserDatas());

        // when
        userEvaluationService.submitEvaluation(evaluationId, submission);

        // then
        verify(userDataService).save(expectedUserDataArgumentCaptor.capture());
        verify(applicationEventPublisher).publishEvent(evaluationSummaryEventArgumentCaptor.capture());
        verify(applicationEventPublisher).publishEvent(updateUserRolesEventArgumentCaptor.capture());
        assertThat(expectedUserDataArgumentCaptor.getValue()).satisfies(userdatas -> {
            List<? extends UserData> userdatasList = userdatas.stream().toList();
            assertThat(userdatasList.get(0).getUserId()).isEqualTo(USER_ID_1);
            assertThat(userdatasList.get(0).getExp()).isEqualTo(USER_1_EXP + Action.MAIN_OBJECTIVE_COMPLETED.getValue());

            assertThat(userdatasList.get(1).getUserId()).isEqualTo(USER_ID_3);
            assertThat(userdatasList.get(1).getExp()).isEqualTo(Action.SPECTACULAR_ACHIEVEMENTS_IN_FIGHT.getValue());
        });

        EvaluationSummaryEvent evaluationSummaryEvent = evaluationSummaryEventArgumentCaptor.getValue();
        UpdateUserRolesEvent updateUserRolesEvent = updateUserRolesEventArgumentCaptor.getValue();

        assertThat(evaluationSummaryEvent.getEvaluationSummary().getPlayers())
                .extracting(EvaluationSummary.SummaryPlayer::getExp)
                .containsExactlyInAnyOrder(
                        USER_1_EXP + Action.MAIN_OBJECTIVE_COMPLETED.getValue(),
                        Action.SPECTACULAR_ACHIEVEMENTS_IN_FIGHT.getValue()
                );

        assertThat(updateUserRolesEvent.getUsersIdsToUpdate()).containsExactlyInAnyOrder(USER_ID_1, USER_ID_3);
    }

    private Map<UserId, UserData> prepareExistingUserDatas()
    {
        return Map.of(
                UserId.of(USER_ID_1), UserData.builder()
                        .userId(USER_ID_1)
                        .exp(USER_1_EXP)
                        .build(),
                UserId.of(USER_ID_2), UserData.builder()
                        .userId(USER_ID_2)
                        .exp(USER_2_EXP)
                        .build()
        );
    }

    private SubmittedUser prepareSubmittedUser(long userId, EnumSet<Action> actions)
    {
        SubmittedUser submittedUser = new SubmittedUser();
        submittedUser.setUserId(String.valueOf(userId));
        submittedUser.setActions(actions);
        return submittedUser;
    }

    private List<SubmittedUser> prepareSubmittedUsers(SubmittedUser... submittedUsers)
    {
        return Arrays.stream(submittedUsers)
                .toList();
    }
}