package pl.tispmc.wolfie.common.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.tispmc.wolfie.common.repository.UserDataRepository;
import pl.tispmc.wolfie.common.model.UserData;

import java.util.Collection;
import java.util.List;

@Service
@AllArgsConstructor
public class UserDataService
{
    private final UserDataRepository userDataRepository;

    public Collection<UserData> findAll()
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

    public void save(List<UserData> userDataList)
    {
        for (UserData userData : userDataList)
        {
            this.userDataRepository.save(userData);
        }
    }
}
