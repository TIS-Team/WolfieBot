package pl.tispmc.wolfie.discord.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStats {
    private String name;
    private String userId;
    private int exp;
    private int level;
    private int appraisalsCount;
    private int reprimandsCount;
    private int specialAwardCount;
    private int missionsPlayed;
}
