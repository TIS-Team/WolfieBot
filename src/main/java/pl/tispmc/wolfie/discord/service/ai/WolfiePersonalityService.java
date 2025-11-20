package pl.tispmc.wolfie.discord.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class WolfiePersonalityService
{
    private static final String PERSONALITIES_DIRECTORY = "classpath:personalities/**";

    private static final Random RANDOM = new Random();
    private final ResourceLoader resourceLoader;

    private String selectedPersonality;

    @PostConstruct
    public void postInit()
    {
        selectNewPersonality();
    }

    @Scheduled(cron = "0 30 4 * * *")
    public void changePersonality()
    {
        selectNewPersonality();
    }

    public String getWolfiePersonality()
    {
        return this.selectedPersonality;
    }

    private void selectNewPersonality()
    {
        try
        {
            Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(this.resourceLoader).getResources(PERSONALITIES_DIRECTORY);
            int randomPersonalityResource = RANDOM.nextInt(resources.length);
            this.selectedPersonality =  resources[randomPersonalityResource].getContentAsString(StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not load wolfie personalities", e);
        }
    }
}
