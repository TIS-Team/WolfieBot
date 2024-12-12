package pl.tispmc.wolfie.discord.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration(proxyBeanMethods = false)
public class BotConfig {

    @Value("${bot-token}")
    private String token;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
