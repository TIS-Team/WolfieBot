package pl.tispmc.wolfie.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.tispmc.wolfie.common.util.DateTimeProvider;

import java.time.Clock;
import java.time.ZoneId;

@Configuration(proxyBeanMethods = false)
public class DateTimeProviderConfig
{
    @Bean
    public DateTimeProvider dateTimeProvider()
    {
        return new DateTimeProvider(Clock.system(ZoneId.of("Europe/Warsaw")));
    }
}
