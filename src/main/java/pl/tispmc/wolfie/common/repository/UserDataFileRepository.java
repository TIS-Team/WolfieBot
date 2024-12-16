package pl.tispmc.wolfie.common.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.tispmc.wolfie.common.model.UserData;
import pl.tispmc.wolfie.common.model.UserId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class UserDataFileRepository implements UserDataRepository
{
    private static final String USER_DATA_FILE = "user-data.json";
    private static final Path USER_DATA_FILE_PATH = Paths.get(".").resolve("data").resolve(USER_DATA_FILE);

    private static final Map<UserId, UserData> cache = new HashMap<>();

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void postConstruct()
    {
        try
        {
            Files.createDirectories(USER_DATA_FILE_PATH.getParent());
            Files.write(USER_DATA_FILE_PATH, objectMapper.writeValueAsBytes(List.of()));

            final List<UserData> userDataList = objectMapper.readValue(USER_DATA_FILE_PATH.toFile(), new TypeReference<>() {});
            cache.putAll(userDataList.stream().collect(Collectors.toMap(userdata -> UserId.of(userdata.getUserId()), userdata -> userdata)));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(UserData userData)
    {
        cache.put(UserId.of(userData.getUserId()), userData);
        CompletableFuture.runAsync(this::saveCacheToFile);
    }

    @Override
    public Map<UserId, UserData> findAll()
    {
        return Map.copyOf(cache);
    }

    @Override
    public UserData find(long userId)
    {
        return cache.get(UserId.of(userId));
    }

    private void saveCacheToFile()
    {
        try
        {
            objectMapper.writeValue(USER_DATA_FILE_PATH.toFile(), this.cache.values());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
