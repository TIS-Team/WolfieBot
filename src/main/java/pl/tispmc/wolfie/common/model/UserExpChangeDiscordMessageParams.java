package pl.tispmc.wolfie.common.model;

import lombok.Value;

import java.util.EnumSet;

@Value(staticConstructor = "of")
public class UserExpChangeDiscordMessageParams
{
    long userId;
    String avatarUrl;
    int expChange;
    int totalExp;
//    int expToNewLevel;
//    int level;
    int missionsPlayed;
    EnumSet<Action> actions;
}