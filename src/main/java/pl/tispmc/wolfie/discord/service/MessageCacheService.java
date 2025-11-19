package pl.tispmc.wolfie.discord.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

@Service
public class MessageCacheService {

    private final Cache<String, Deque<String>> messageCache;
    private static final int MAX_HISTORY_SIZE = 5; // 2-3 messages means a bit more context, so 5 entries (user+bot)

    public MessageCacheService() {
        this.messageCache = CacheBuilder.newBuilder()
                .maximumSize(1000) // Max number of users with history
                .expireAfterAccess(10, TimeUnit.MINUTES) // Evict after 10 minutes of inactivity
                .build();
    }

    public void addMessage(String userId, String message) {
        Deque<String> history = messageCache.getIfPresent(userId);
        if (history == null) {
            history = new LinkedList<>();
        }
        history.add(message);
        while (history.size() > MAX_HISTORY_SIZE) {
            history.poll();
        }
        messageCache.put(userId, history);
    }

    public Deque<String> getHistory(String userId) {
        return messageCache.getIfPresent(userId);
    }
}
