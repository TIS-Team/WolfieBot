package pl.tispmc.wolfie.common.repository;

import pl.tispmc.wolfie.common.model.UserData;

import java.util.List;

public interface UserDataRepository
{
    void save(UserData userData);

    List<UserData> findAll();

    UserData find(long userId);
}
