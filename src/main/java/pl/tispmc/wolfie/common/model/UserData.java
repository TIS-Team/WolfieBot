package pl.tispmc.wolfie.common.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
@Value
public class UserData
{
    String name;
    long userId;
    int exp;
    int appraisalsCount;
    int reprimandsCount;
    int specialAwardCount;
    int missionsPlayed;
    LocalDateTime joinDate;

    @Builder.Default
    ExpClaims expClaims = ExpClaims.builder().build();

    @Builder.Default
    List<Award> awards = new ArrayList<>();

    @Value
    @Builder(toBuilder = true)
    public static class ExpClaims
    {
        int dailyExpStreak;
        int dailyExpStreakMaxRecord;
        LocalDateTime lastDailyExpClaim;
    }
}