package pl.tispmc.wolfie.web.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.tispmc.wolfie.common.dto.FleetShipDto;
import pl.tispmc.wolfie.common.fleetyards.FleetYardsCatalog;
import pl.tispmc.wolfie.common.fleetyards.model.FleetYardsModel;
import pl.tispmc.wolfie.common.model.UserShips;
import pl.tispmc.wolfie.common.service.FleetService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Slf4j
@RestController
@RequestMapping("/api/v1/fleet")
@RequiredArgsConstructor
public class FleetRestController
{
    private final FleetService fleetService;
    private final FleetYardsCatalog fleetYardsCatalog;

    @GetMapping
    public List<FleetShipDto> getFleet()
    {
        log.info("Getting fleet");
        Map<String, List<String>> ownersBySlug = new TreeMap<>();
        for (UserShips userShips : fleetService.findAll().values())
        {
            for (String slug : userShips.getShips())
            {
                ownersBySlug.computeIfAbsent(slug, key -> new ArrayList<>()).add(userShips.getName());
            }
        }

        return ownersBySlug.entrySet().stream()
                .map(entry -> toFleetShipDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    private FleetShipDto toFleetShipDto(String slug, List<String> owners)
    {
        FleetYardsModel model = fleetYardsCatalog.findBySlug(slug).orElse(null);
        if (model == null)
            return new FleetShipDto(slug, slug, "", "", 0, "", "", "", "", owners);

        return new FleetShipDto(
                slug,
                model.name(),
                Optional.ofNullable(model.manufacturer()).map(FleetYardsModel.Manufacturer::name).orElse(""),
                Optional.ofNullable(model.focus()).or(() -> Optional.ofNullable(model.classificationLabel())).orElse(""),
                Optional.ofNullable(model.metrics()).map(FleetYardsModel.Metrics::length).orElse(0d),
                Optional.ofNullable(model.metrics()).map(FleetYardsModel.Metrics::lengthLabel).orElse(""),
                Optional.ofNullable(model.crew()).map(FleetYardsModel.Crew::maxLabel).orElse(""),
                Optional.ofNullable(model.media())
                        .map(FleetYardsModel.Media::storeImage)
                        .map(storeImage -> Optional.ofNullable(storeImage.smallUrl()).orElse(storeImage.url()))
                        .orElse(""),
                Optional.ofNullable(model.links()).map(FleetYardsModel.Links::storeUrl).orElse(""),
                owners);
    }
}
