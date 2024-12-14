package pl.tispmc.wolfie.discord.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration(proxyBeanMethods = false)
public class BotConfig {

    @Value("${bot-token}")
    private String token;
}
