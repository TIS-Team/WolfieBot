package pl.tispmc.wolfie.common.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum Rank {
    RECRUIT(1265660921829785685L, "Rekrut", 0),
    PRIVATE(1266326384033402901L, "Szeregowy", 150),
    SENIOR_PRIVATE(1266327271548125236L, "Starszy Szeregowy", 400),
    CORPORAL(1266326759331332158L, "Kapral", 900),
    SERGEANT(1271068587481628753L, "Sierżant", 1600),
    SECOND_LIEUTENANT(1271068328437485662L, "Podporucznik", 2500),
    LIEUTENANT(1266327570178379858L, "Porucznik", 3600),
    CAPTAIN(1265703592577208342L, "Kapitan", 4900),
    MAJOR(1266327793575133255L, "Major", 6800),
    GENERAL(1266327837426843669L, "Generał", 10000);

    private static final Map<Long, Rank> RANK_MAP = Arrays.stream(Rank.values())
            .collect(Collectors.toUnmodifiableMap(Rank::getId, Function.identity()));

    private final long id;
    private final String name;
    private final int exp;

    Rank(long id, String name, int exp) {
        this.id = id;
        this.name = name;
        this.exp = exp;
    }

    public Rank next() {
        return values()[(this.ordinal() + 1) % values().length];
    }

    public static Map<Long, Rank> getRankMap()
    {
        return RANK_MAP;
    }
}