package pl.tispmc.wolfie.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
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
