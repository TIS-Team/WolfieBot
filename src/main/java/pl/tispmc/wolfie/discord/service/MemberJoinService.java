package pl.tispmc.wolfie.discord.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.UserDataCreator;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.service.UserDataService;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class MemberJoinService
{
    private final UserDataService userDataService;

    public void setDataOnJoin(Member member)
    {
        Optional<UserData> userData = Optional.ofNullable(userDataService.find(member.getIdLong()));

        // First join
        if (userData.isEmpty())
        {
            log.info("Creating user data for user id: {}", member.getId());
            userDataService.save(UserDataCreator.createUserData(member).toBuilder()
                    .joinDate(LocalDateTime.now())
                    .build());
        }
    }
}
