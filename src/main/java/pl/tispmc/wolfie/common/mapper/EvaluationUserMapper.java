package pl.tispmc.wolfie.common.mapper;

import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.Evaluation;
import pl.tispmc.wolfie.common.model.User;

@Component
public class EvaluationUserMapper
{
    public Evaluation.EvaluationUser map(User user)
    {
        if (user == null)
            return null;

        return Evaluation.EvaluationUser.builder()
                .id(user.getId())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
