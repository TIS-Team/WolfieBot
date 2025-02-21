package pl.tispmc.wolfie.common.event.model;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.context.ApplicationEvent;
import pl.tispmc.wolfie.common.model.Rank;

@Value
@EqualsAndHashCode(callSuper = false)
public class RankChangedEvent extends ApplicationEvent
{
    String userName;
    Rank oldRank;
    Rank newRank;

    public RankChangedEvent(Object source, String userName, Rank oldRank, Rank newRank)
    {
        super(source);
        this.userName = userName;
        this.oldRank = oldRank;
        this.newRank = newRank;
    }

    public Rank getNewRank()
    {
        return newRank;
    }

    public Rank getOldRank()
    {
        return oldRank;
    }

    public String getUserName()
    {
        return userName;
    }
}
