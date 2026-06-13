package pl.tispmc.wolfie.common.fleetyards;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.tispmc.wolfie.common.fleetyards.model.FleetYardsModel;
import pl.tispmc.wolfie.common.fleetyards.model.FleetYardsModelsResponse;

import java.util.ArrayList;
import java.util.List;

@Component
public class FleetYardsClient
{
    private static final int PAGE_SIZE = 200;

    private final RestClient restClient;

    public FleetYardsClient(@Value("${fleetyards.url}") String fleetYardsUrl)
    {
        this.restClient = RestClient.builder().baseUrl(fleetYardsUrl).build();
    }

    public List<FleetYardsModel> fetchAllModels()
    {
        List<FleetYardsModel> models = new ArrayList<>();
        int page = 1;

        while (true)
        {
            final int currentPage = page;
            FleetYardsModelsResponse response = this.restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/models")
                            .queryParam("perPage", PAGE_SIZE)
                            .queryParam("page", currentPage)
                            .build())
                    .retrieve()
                    .body(FleetYardsModelsResponse.class);

            if (response == null || response.items() == null || response.items().isEmpty())
                break;

            models.addAll(response.items());

            if (response.items().size() < PAGE_SIZE)
                break;

            page++;
        }
        return models;
    }
}
