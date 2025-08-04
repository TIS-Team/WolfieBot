package pl.tispmc.wolfie.discord.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "bot.join-roles")
@Data
public class JoinRolesConfigurationProperties
{
    private List<Long> add;
}
