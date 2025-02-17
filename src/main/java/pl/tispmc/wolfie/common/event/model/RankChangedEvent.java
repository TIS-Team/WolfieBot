package pl.tispmc.wolfie.common.event.model;

import org.springframework.context.ApplicationEvent;
import pl.tispmc.wolfie.common.model.Rank;

public class RankChangedEvent extends ApplicationEvent
{
    private String userName;
    private Rank oldRank;
    private Rank newRank;

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
