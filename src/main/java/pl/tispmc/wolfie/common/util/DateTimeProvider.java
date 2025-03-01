package pl.tispmc.wolfie.common.util;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class DateTimeProvider
{
    private static final Clock CLOCK = Clock.system(ZoneId.of("Europe/Warsaw"));

    public Instant currentInstant()
    {
        return Instant.now(CLOCK);
    }

    public LocalDateTime currentLocalDateTime()
    {
        return LocalDateTime.from(CLOCK.instant());
    }
}
