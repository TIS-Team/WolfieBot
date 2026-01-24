package pl.tispmc.wolfie.discord.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wolfie.ai")
@Getter
@Setter
public class AiConfigurationProperties
{
    private String systemPromptFile;
    private String knowledgeBaseFile;
    private PersonalityConfig personality;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalityConfig
    {
        private ChangingConfig changing;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ChangingConfig
        {
            private boolean enabled;
            private String cron;
        }
    }
}
