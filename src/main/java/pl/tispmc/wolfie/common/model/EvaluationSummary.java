package pl.tispmc.wolfie.common.model;

import lombok.Builder;
import lombok.Value;

import java.util.EnumSet;
import java.util.List;

@Value
@Builder
public class EvaluationSummary
{
    String missionName;
    List<SummaryPlayer> players;
    SummaryPlayer missionMaker;

    @Value
    @Builder
    public static class SummaryPlayer
    {
        long id;
        String name;
        String avatarUrl;
        EnumSet<Action> actions;
        int exp;
        int expChange;
        int missionsPlayed;
    }
}