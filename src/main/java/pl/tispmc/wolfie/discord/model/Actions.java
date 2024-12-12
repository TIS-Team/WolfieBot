package pl.tispmc.wolfie.discord.model;

import java.util.List;
import java.util.Arrays;

public class Actions
{

    public static class Action {
        private String name;
        private int value;

        public Action(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }

    public static final List<Action> PRAISE_ACTIONS = Arrays.asList(
            new Action("Wykonanie zadania głównego", 12),
            new Action("Wykonanie zadania pobocznego", 6),
            new Action("Efektywne wykorzystanie zasobów", 9),
            new Action("Zorganizowane dowodzenie", 10),
            new Action("Precyzyjne wykonanie taktyki", 7),
            new Action("Wsparcie drużynowe", 7),
            new Action("Szybka reakcja na zmianę sytuacji", 9),
            new Action("Udział w strategii i planowaniu", 5),
            new Action("Właściwe używanie sprzętu", 6),
            new Action("Spektakularne osiągnięcia w walce", 15),
            new Action("Efektywna współpraca zespołowa", 8),
            new Action("Zachowanie wysokiej morale drużyny", 6),
            new Action("Doskonałe wykorzystanie pojazdów", 10),
            new Action("Innowacyjne podejście do problemów", 8),
            new Action("Ocalenie innych graczy w trudnej sytuacji", 12)
    );

    public static final List<Action> REPRIMAND_ACTIONS = Arrays.asList(
            new Action("Porażka zadania głównego", -20),
            new Action("Porażka zadania pobocznego", -10),
            new Action("Zbrodnie wojenne", -30),
            new Action("Porzucenie Sprzętu", -10),
            new Action("Ignorowanie rozkazów dowódcy", -15),
            new Action("Stwarzanie zagrożenia dla innych graczy", -25),
            new Action("Niewłaściwe użycie sprzętu", -10),
            new Action("Lekceważenie organizatorów i sędziów", -25),
            new Action("Celowe przeszkadzanie innym graczom", -15),
            new Action("Spektakularna porażka", -40),
            new Action("Brak komunikacji z drużyną", -8),
            new Action("Utrata ważnych zasobów", -12),
            new Action("Unikanie walki w kluczowym momencie", -10),
            new Action("Niepotrzebne ryzyko narażające drużynę", -12),
            new Action("Nieprzestrzeganie zasad scenariusza", -15),
            new Action("Wykorzystanie exploita gry", -20),
            new Action("Negatywne zachowanie wobec innych graczy", -18)
    );
}
