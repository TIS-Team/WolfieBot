package pl.tispmc.wolfie.common.dto;

import java.util.List;

public record FleetShipDto(
        String slug,
        String name,
        String manufacturer,
        String focus,
        double length,
        String lengthLabel,
        String crewLabel,
        String image,
        String storeUrl,
        List<String> owners) {}
