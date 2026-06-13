package pl.tispmc.wolfie.common.repository;

import pl.tispmc.wolfie.common.model.UserId;
import pl.tispmc.wolfie.common.model.UserShips;

import java.util.Map;

public interface UserShipsRepository
{
    void save(UserShips userShips);

    Map<UserId, UserShips> findAll();

    UserShips find(long userId);
}
