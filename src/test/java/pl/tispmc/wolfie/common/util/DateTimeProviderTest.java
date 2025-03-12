package pl.tispmc.wolfie.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DateTimeProviderTest
{
    private static final Instant INSTANT = ZonedDateTime.of(
            LocalDate.of(2022, 2, 12),
            LocalTime.of(14, 15),
            ZoneId.of("Europe/Warsaw")
    ).toInstant();

    private final DateTimeProvider dateTimeProvider = new DateTimeProvider(Clock.fixed(INSTANT, ZoneId.of("Europe/Warsaw")));

    @Test
    void shouldReturnLocalDateTime() {
        // given
        // when
        ZonedDateTime zonedDateTime = dateTimeProvider.currentZonedDateTime();

        // then
        assertThat(zonedDateTime).isEqualTo(ZonedDateTime.of(LocalDate.of(2022, 2, 12),
                LocalTime.of(14, 15),
                ZoneId.of("Europe/Warsaw")));
    }
}