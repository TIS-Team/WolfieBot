package pl.tispmc.wolfie.common.event.model;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.context.ApplicationEvent;
import pl.tispmc.wolfie.common.model.Rank;

@Value
@EqualsAndHashCode(callSuper = false)
public class RankChangedEvent extends ApplicationEvent
{
    RankChangedEventData data;

    public RankChangedEvent(Object source, RankChangedEventData data)
    {
        super(source);
        this.data = data;
    }

    @Value
    public static class RankChangedEventData
    {
        String username;
        String avatarUrl;
        Rank oldRank;
        Rank newRank;
    }
}
