package pl.tispmc.wolfie.common.mapper;

import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.Evaluation;
import pl.tispmc.wolfie.common.model.User;
import pl.tispmc.wolfie.common.model.UserData;

import java.util.Optional;

@Component
public class EvaluationUserMapper
{
    public Evaluation.EvaluationUser map(User user, UserData userData)
    {
        if (user == null)
            return null;

        return Evaluation.EvaluationUser.builder()
                .id(user.getId())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .exp(Optional.ofNullable(userData).map(UserData::getExp).orElse(0))
                .build();
    }
}
