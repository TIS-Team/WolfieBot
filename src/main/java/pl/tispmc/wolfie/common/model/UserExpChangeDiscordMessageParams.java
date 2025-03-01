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
    int missionsPlayed;
    EnumSet<Action> actions;
}