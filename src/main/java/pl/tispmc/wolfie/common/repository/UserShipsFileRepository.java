package pl.tispmc.wolfie.common.repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.tispmc.wolfie.common.model.UserId;
import pl.tispmc.wolfie.common.model.UserShips;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

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
@RequiredArgsConstructor
public class UserShipsFileRepository implements UserShipsRepository
{
    private static final String USER_SHIPS_FILE = "user-ships.json";
    private static final Path USER_SHIPS_FILE_PATH = Paths.get(".").resolve("data").resolve(USER_SHIPS_FILE);

    private static final Map<UserId, UserShips> cache = new HashMap<>();

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void postConstruct()
    {
        try
        {
            Files.createDirectories(USER_SHIPS_FILE_PATH.getParent());
            if (Files.notExists(USER_SHIPS_FILE_PATH))
            {
                Files.write(USER_SHIPS_FILE_PATH, objectMapper.writeValueAsBytes(List.of()));
            }

            final List<UserShips> userShipsList = objectMapper.readValue(USER_SHIPS_FILE_PATH.toFile(), new TypeReference<>() {});
            cache.putAll(userShipsList.stream().collect(Collectors.toMap(userShips -> UserId.of(userShips.getUserId()), userShips -> userShips)));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(UserShips userShips)
    {
        cache.put(UserId.of(userShips.getUserId()), userShips);
        scheduleSaveCacheToFile();
    }

    @Override
    public Map<UserId, UserShips> findAll()
    {
        return Map.copyOf(cache);
    }

    @Override
    public UserShips find(long userId)
    {
        return cache.get(UserId.of(userId));
    }

    private void scheduleSaveCacheToFile()
    {
        CompletableFuture.runAsync(this::saveCacheToFile);
    }

    private void saveCacheToFile()
    {
        try
        {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(USER_SHIPS_FILE_PATH.toFile(), cache.values());
        }
        catch (JacksonException e)
        {
            throw new RuntimeException(e);
        }
    }
}
