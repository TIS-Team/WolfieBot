package pl.tispmc.wolfie.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserData
{
    private String name;
    private long userId;
    private int exp;
    private int level;
    private int appraisalsCount;
    private int reprimandsCount;
    private int specialAwardCount;
    private int missionsPlayed;
}
