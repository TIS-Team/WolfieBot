package pl.tispmc.wolfie.common.model;

import java.util.List;
import java.util.Map;

public class Actions {

    public record Action(String name, int value) {}

    public static final Map<String, List<Action>> CATEGORIZED_ACTIONS = Map.of(
            "Misje", List.of(
                    new Action("Wykonanie zadania głównego", 12),
                    new Action("Wykonanie zadania pobocznego", 6),
                    new Action("Efektywne wykorzystanie zasobów", 9),
                    new Action("Szybka reakcja na zmianę sytuacji", 9),
                    new Action("Unikanie walki w kluczowym momencie", -10),
                    new Action("Porażka zadania głównego", -20),
                    new Action("Porażka zadania pobocznego", -10)
            ),
            "Drużyna", List.of(
                    new Action("Zorganizowane dowodzenie", 10),
                    new Action("Wsparcie drużynowe", 7),
                    new Action("Efektywna współpraca zespołowa", 8),
                    new Action("Zachowanie wysokiej morale drużyny", 6),
                    new Action("Ignorowanie rozkazów dowódcy", -15),
                    new Action("Stwarzanie zagrożenia dla innych graczy", -25),
                    new Action("Brak komunikacji z drużyną", -8)
            ),
            "Sprzęt", List.of(
                    new Action("Właściwe używanie sprzętu", 6),
                    new Action("Doskonałe wykorzystanie pojazdów", 10),
                    new Action("Porzucenie Sprzętu", -10),
                    new Action("Niewłaściwe użycie sprzętu", -10),
                    new Action("Utrata ważnych zasobów", -12)
            ),
            "Innowacyjność", List.of(
                    new Action("Innowacyjne podejście do problemów", 8),
                    new Action("Spektakularne osiągnięcia w walce", 15),
                    new Action("Spektakularna porażka", -40)
            ),
            "Zasady", List.of(
                    new Action("Nieprzestrzeganie zasad scenariusza", -15),
                    new Action("Wykorzystanie exploita gry", -20),
                    new Action("Negatywne zachowanie wobec innych graczy", -18),
                    new Action("Lekceważenie organizatorów i sędziów", -25)
            )
    );
}
