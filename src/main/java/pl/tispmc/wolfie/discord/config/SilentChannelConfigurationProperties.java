package pl.tispmc.wolfie.discord.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "wolfie.silent-channel")
@Getter
@Setter
public class SilentChannelConfigurationProperties
{
    private Target target;

    @Getter
    @Setter
    public static class Target
    {
        boolean targetWhenNoRoles;
        List<String> userRoleIds;
    }
}
