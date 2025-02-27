package pl.tispmc.wolfie.common;

import net.dv8tion.jda.api.entities.Member;
import pl.tispmc.wolfie.common.model.UserData;

import java.util.ArrayList;

public class UserDataCreator
{
    public static UserData createUserData(Member member)
    {
        return UserData.builder()
                .userId(member.getIdLong())
                .name(member.getEffectiveName())
                .awards(new ArrayList<>())
                .build();
    }

    private UserDataCreator()
    {
        throw new UnsupportedOperationException("You should not instantiate this class.");
    }
}
