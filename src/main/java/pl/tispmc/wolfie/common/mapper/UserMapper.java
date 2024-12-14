package pl.tispmc.wolfie.common.mapper;

import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.User;

@Component
public class UserMapper
{
    public User map(net.dv8tion.jda.api.entities.User user)
    {
        if (user == null)
            return null;

        return User.builder()
                .id(user.getIdLong())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
