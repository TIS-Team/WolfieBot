package pl.tispmc.wolfie.common.model;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public enum Action
{
    MAIN_OBJECTIVE_COMPLETED("Wykonanie zadania głównego", 12),
    SECONDARY_OBJECTIVE_COMPLETED("Wykonanie zadania pobocznego", 6),
    EFFECTIVE_RESOURCES_USAGE("Efektywne wykorzystanie zasobów", 9),
    FAST_REACTION_ON_SITUATION("Szybka reakcja na zmianę sytuacji", 9),
    AVOIDING_FIGHT_IN_CRUCIAL_MOMENT("Unikanie walki w kluczowym momencie", -10),
    MAIN_OBJECTIVE_FAILURE("Porażka zadania głównego", -20),
    SECONDARY_OBJECTIVE_FAILURE("Porażka zadania pobocznego", -10),

    ORGANIZED_LEADERSHIP("Zorganizowane dowodzenie", 10),
    TEAM_SUPPORT("Wsparcie drużynowe", 7),
    EFFECTIVE_TEAMWORK("Efektywna współpraca zespołowa", 8),
    KEEP_HIGH_TEAM_MORALE("Zachowanie wysokich morale drużyny", 6),
    IGNORING_LEADER_ORDERS("Ignorowanie rozkazów dowódcy", -15),
    CREATING_DANGER_CLOSE_FOR_OTHERS("Stwarzanie zagrożenia dla innych graczy", -25),
    NO_COMMUNICATION_WITH_TEAM("Brak komunikacji z drużyną", -8),

    PROPER_EQUIPMENT_USAGE("Właściwe używanie sprzętu", 6),
    PROPER_VEHICLE_USAGE("Doskonałe wykorzystanie pojazdów", 10),
    ABANDONED_EQUIPMENT("Porzucenie Sprzętu", -10),
    IMPROPER_EQUIPMENT_USAGE("Niewłaściwe użycie sprzętu", -10),
    IMPORTANT_RESOURCES_LOST("Utrata ważnych zasobów", -12),

    INNOVATIVE_PROBLEM_SOLVING("Innowacyjne podejście do problemów", 8),
    SPECTACULAR_ACHIEVEMENTS_IN_FIGHT("Spektakularne osiągnięcia w walce", 35),
    SPECTACULAR_FAILURE("Spektakularna porażka", -40),

    BREAKING_SCENARIO_RULES("Nieprzestrzeganie zasad scenariusza", -15),
    EXPLOITING("Wykorzystanie exploita gry", -20),
    IMPROPER_BEHAVIOUR_TOWARDS_OTHER_PLAYERS("Negatywne zachowanie wobec innych graczy", -18),
    DISREGARD_ORGANIZERS_AND_JUDGES("Lekceważenie organizatorów i sędziów", -25);

    public static final Map<String, List<Action>> CATEGORIZED_ACTIONS = Map.of(
            "Misje", List.of(
                    MAIN_OBJECTIVE_COMPLETED,
                    SECONDARY_OBJECTIVE_COMPLETED,
                    EFFECTIVE_RESOURCES_USAGE,
                    FAST_REACTION_ON_SITUATION,
                    AVOIDING_FIGHT_IN_CRUCIAL_MOMENT,
                    MAIN_OBJECTIVE_FAILURE,
                    SECONDARY_OBJECTIVE_FAILURE
            ),
            "Drużyna", List.of(
                    ORGANIZED_LEADERSHIP,
                    TEAM_SUPPORT,
                    EFFECTIVE_TEAMWORK,
                    KEEP_HIGH_TEAM_MORALE,
                    IGNORING_LEADER_ORDERS,
                    CREATING_DANGER_CLOSE_FOR_OTHERS,
                    NO_COMMUNICATION_WITH_TEAM
            ),
            "Sprzęt", List.of(
                    PROPER_EQUIPMENT_USAGE,
                    PROPER_VEHICLE_USAGE,
                    ABANDONED_EQUIPMENT,
                    IMPROPER_EQUIPMENT_USAGE,
                    IMPORTANT_RESOURCES_LOST
            ),
            "Innowacyjność", List.of(
                    INNOVATIVE_PROBLEM_SOLVING,
                    SPECTACULAR_ACHIEVEMENTS_IN_FIGHT,
                    SPECTACULAR_FAILURE
            ),
            "Zasady", List.of(
                    BREAKING_SCENARIO_RULES,
                    EXPLOITING,
                    IMPORTANT_RESOURCES_LOST,
                    DISREGARD_ORGANIZERS_AND_JUDGES
            )
    );

    private final String displayName;
    private final int value;

    Action(final String displayName, final int value)
    {
        this.displayName = displayName;
        this.value = value;
    }
}
