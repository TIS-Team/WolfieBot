package pl.tispmc.wolfie.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder
public class UserData
{
    String name;
    long userId;
    int exp;
    int level;
    int appraisalsCount;
    int reprimandsCount;
    int specialAwardCount;
    int missionsPlayed;
}
