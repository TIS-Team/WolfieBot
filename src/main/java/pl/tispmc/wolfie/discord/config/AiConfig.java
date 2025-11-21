package pl.tispmc.wolfie.discord.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai")
@Getter
@Setter
public class AiConfig
{
    private String systemPromptFile;
    private String knowledgeBaseFile;
}
