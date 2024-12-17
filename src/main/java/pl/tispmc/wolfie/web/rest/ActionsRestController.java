package pl.tispmc.wolfie.web.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.tispmc.wolfie.common.dto.ActionDto;
import pl.tispmc.wolfie.common.model.Action;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/actions")
public class ActionsRestController {

    @GetMapping
    public Map<String, List<ActionDto>> getCategorizedActions() {
        return Action.CATEGORIZED_ACTIONS.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(action -> new ActionDto(action.name(), action.getDisplayName(), action.getValue()))
                                .collect(Collectors.toList())
                ));
    }
}
