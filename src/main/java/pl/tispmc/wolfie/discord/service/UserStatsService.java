package pl.tispmc.wolfie.discord.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import pl.tispmc.wolfie.discord.model.UserStats;

import java.io.File;
import java.io.IOException;

@Service
public class UserStatsService {
    private static final UserStats DEFAULT_STATS = new UserStats("NaN", "NaN", 0, 0, 0,0 ,0 ,0);
    private static final String USER_STATS_FILE = "src/main/resources/data/user-data.json";
    private final ObjectMapper objectMapper;

    public UserStatsService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UserStats getUserStats(String userId) {
        try {
            UserStats[] stats = objectMapper.readValue(new File(USER_STATS_FILE), UserStats[].class);
            for (UserStats stat : stats) {
                if (stat.getUserId().equals(userId)) {
                    return stat;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load user stats", e);
        }
        return DEFAULT_STATS; // Default stats (just display)
    }
}
