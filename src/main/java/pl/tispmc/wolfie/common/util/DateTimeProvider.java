package pl.tispmc.wolfie.common.util;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@RequiredArgsConstructor
public class DateTimeProvider
{
    private final Clock clock;

    public Instant currentInstant()
    {
        return Instant.now(clock);
    }

    public ZonedDateTime currentZonedDateTime()
    {
        return ZonedDateTime.now(clock);
    }

    public ZonedDateTime withCorrectZone(LocalDateTime localDateTime)
    {
        return localDateTime.atZone(clock.getZone());
    }

    public ZoneId getZoneId()
    {
        return clock.getZone();
    }
}
