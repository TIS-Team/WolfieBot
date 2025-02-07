package pl.tispmc.wolfie.common.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.tispmc.wolfie.common.model.UserId;
import pl.tispmc.wolfie.common.repository.UserDataRepository;
import pl.tispmc.wolfie.common.model.UserData;

import java.util.Collection;
import java.util.Map;

@Service
@AllArgsConstructor
public class UserDataService
{
    private final UserDataRepository userDataRepository;

    public Map<UserId, UserData> findAll()
    {
        return this.userDataRepository.findAll();
    }

    public UserData find(long userId)
    {
        return this.userDataRepository.find(userId);
    }

    public void save(UserData userData)
    {
        this.userDataRepository.save(userData);
    }

    public void save(Collection<UserData> userDataList)
    {
        this.userDataRepository.saveAll(userDataList);
    }
}
