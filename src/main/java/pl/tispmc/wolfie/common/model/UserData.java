package pl.tispmc.wolfie.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class UserData
{
    String name;
    long userId;
    int exp;
    int appraisalsCount;
    int reprimandsCount;
    int specialAwardCount;
    int missionsPlayed;

    ExpClaims expClaims;

    @Value
    @Builder(toBuilder = true)
    public static class ExpClaims
    {
        int dailyExpStreak;
        LocalDateTime lastDailyExpClaim;
    }
}
