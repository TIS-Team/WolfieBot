package pl.tispmc.wolfie.common.service;

import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.Rank;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RankService
{
    public Map<Long, Rank> getSupportedRanks()
    {
        return Arrays.stream(Rank.values())
                .collect(Collectors.toMap(Rank::getId, Function.identity()));
    }
}
