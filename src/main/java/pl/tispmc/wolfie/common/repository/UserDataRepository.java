package pl.tispmc.wolfie.common.repository;

import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.model.UserId;

import java.util.Collection;
import java.util.Map;

public interface UserDataRepository
{
    void save(UserData userData);

    Map<UserId, UserData> findAll();

    UserData find(long userId);

    void saveAll(Collection<UserData> userDataList);
}
