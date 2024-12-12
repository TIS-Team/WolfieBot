package pl.tispmc.wolfie.discord.data;

import java.util.List;
import java.util.Arrays;

public class Ranks {

    public static class Rank {
        private String name;
        private String id;
        private int minExp;

        public Rank(String name, String id, int minExp) {
            this.name = name;
            this.id = id;
            this.minExp = minExp;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public int getExp() {
            return minExp;
        }
    }

    public static final List<Rank> RANKS = Arrays.asList(
            new Rank("Rekrut", "1265660921829785685", 0),
            new Rank("Szeregowy", "1266326384033402901", 150),
            new Rank("Starszy Szeregowy", "1266327271548125236", 400),
            new Rank("Kapral", "1266326759331332158", 900),
            new Rank("Sierzant", "1271068587481628753", 1600),
            new Rank("Podporucznik", "1271068328437485662", 2500),
            new Rank("Porucznik", "1266327570178379858", 3600),
            new Rank("Kapitan", "1265703592577208342", 4900),
            new Rank("Major", "1266327793575133255", 6400),
            new Rank("General", "1266327837426843669", 8100)
    );
}
