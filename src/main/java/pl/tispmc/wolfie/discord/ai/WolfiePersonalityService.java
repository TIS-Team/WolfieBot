package pl.tispmc.wolfie.discord.ai;

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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class WolfiePersonalityService
{
    private static final String PERSONALITIES_DIRECTORY = "classpath:personalities/**";

    private static final Random RANDOM = new Random();
    private final ResourceLoader resourceLoader;

    private String selectedPersonalityData;

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
        return this.selectedPersonalityData;
    }

    public List<String> getAvailablePersonalitiesNames()
    {
        return Arrays.stream(getPersonalityResources())
                .map(Resource::getFilename)
                .filter(Objects::nonNull)
                .map(name -> name.substring(0, name.lastIndexOf(".")))
                .toList();
    }

    private void selectNewPersonality()
    {
        try
        {
            Resource[] resources = getPersonalityResources();
            int randomPersonalityResource = RANDOM.nextInt(resources.length);
            Resource personalityResource = resources[randomPersonalityResource];
            this.selectedPersonalityData = personalityResource.getContentAsString(StandardCharsets.UTF_8);
            log.info("Selected personality: {}", personalityResource.getFilename());
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not load wolfie personalities", e);
        }
    }

    private Resource[] getPersonalityResources()
    {
        try
        {
            return ResourcePatternUtils.getResourcePatternResolver(this.resourceLoader).getResources(PERSONALITIES_DIRECTORY);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not load wolfie personalities", e);
        }
    }

    public void setPersonality(String personality)
    {
        try
        {
            this.selectedPersonalityData = Arrays.stream(getPersonalityResources())
                    .filter(resource -> resource.getFilename().startsWith(personality))
                    .findFirst()
                    .orElseThrow()
                    .getContentAsString(StandardCharsets.UTF_8);
            log.info("Selected personality: {}", personality);
        }
        catch (IOException e)
        {
            throw new RuntimeException(format("Could not set personality %s", personality), e);
        }
    }
}
