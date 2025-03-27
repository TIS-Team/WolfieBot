package pl.tispmc.wolfie.common.model;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RankTest {

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            nullValues = "null",
            value = {
                    "RECRUIT | PRIVATE",
                    "SERGEANT | SECOND_LIEUTENANT",
                    "GENERAL | null"
            }
    )
    void shouldGetNextRank(Rank currentRank, Rank expectedRank) {
        // given
        // when
        // then
        assertThat(currentRank.next()).isEqualTo(expectedRank);
    }
}