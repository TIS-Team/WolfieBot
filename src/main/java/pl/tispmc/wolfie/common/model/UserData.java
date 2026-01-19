package pl.tispmc.wolfie.common.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
@Value
@Data
@AllArgsConstructor
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
    @Data
    @AllArgsConstructor
    public static class ExpClaims
    {
        int dailyExpStreak;
        int dailyExpStreakMaxRecord;
        LocalDateTime lastDailyExpClaim;
    }
}