package pl.tispmc.wolfie.discord.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Getter
@Configuration(proxyBeanMethods = false)
@DependsOn("defaultConfigGenerator")
public class BotConfig {

    @Value("${bot-token}")
    private String token;
}
