package pl.tispmc.wolfie.common.service;

import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.Rank;

import java.util.Map;

@Component
public class RankService
{
    public Map<Long, Rank> getSupportedRanks()
    {
        return Rank.getRankMap();
    }
}
