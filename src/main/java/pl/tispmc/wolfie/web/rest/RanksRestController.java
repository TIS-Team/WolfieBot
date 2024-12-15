package pl.tispmc.wolfie.web.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.tispmc.wolfie.common.dto.RankDto;
import pl.tispmc.wolfie.common.model.Rank;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ranks")
public class RanksRestController {

    @GetMapping
    public List<RankDto> getAllRanks() {
        // Convert Rank enum values to DTOs
        return Arrays.stream(Rank.values())
                .map(r -> new RankDto(r.getId(), r.getName(), r.getExp()))
                .collect(Collectors.toList());
    }
}
