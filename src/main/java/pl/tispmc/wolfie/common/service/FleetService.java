package pl.tispmc.wolfie.common.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.tispmc.wolfie.common.model.UserId;
import pl.tispmc.wolfie.common.model.UserShips;
import pl.tispmc.wolfie.common.repository.UserShipsRepository;

import java.util.Map;

@Service
@AllArgsConstructor
public class FleetService
{
    private final UserShipsRepository userShipsRepository;

    public Map<UserId, UserShips> findAll()
    {
        return this.userShipsRepository.findAll();
    }

    public UserShips find(long userId)
    {
        return this.userShipsRepository.find(userId);
    }

    public void save(UserShips userShips)
    {
        this.userShipsRepository.save(userShips);
    }
}
