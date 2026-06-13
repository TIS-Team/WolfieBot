package pl.tispmc.wolfie.common.fleetyards;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.tispmc.wolfie.common.fleetyards.model.FleetYardsModel;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * In-memory catalog of all Star Citizen ship models known to FleetYards.
 *
 * The catalog is loaded on application startup and refreshed once a day.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FleetYardsCatalog
{
    private final FleetYardsClient fleetYardsClient;

    private volatile Map<String, FleetYardsModel> modelsBySlug = Map.of();

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 6 * * *")
    public void refresh()
    {
        log.info("Refreshing FleetYards ship catalog");
        try
        {
            List<FleetYardsModel> models = this.fleetYardsClient.fetchAllModels();
            this.modelsBySlug = models.stream()
                    .collect(Collectors.toUnmodifiableMap(FleetYardsModel::slug, Function.identity(), (first, second) -> first));
            log.info("FleetYards ship catalog refreshed. Loaded {} ship models", this.modelsBySlug.size());
        }
        catch (Exception exception)
        {
            log.error("Could not refresh FleetYards ship catalog: {}", exception.getMessage(), exception);
        }
    }

    public boolean isEmpty()
    {
        return this.modelsBySlug.isEmpty();
    }

    public Optional<FleetYardsModel> findBySlug(String slug)
    {
        return Optional.ofNullable(this.modelsBySlug.get(slug));
    }

    public Optional<FleetYardsModel> findByName(String name)
    {
        return this.modelsBySlug.values().stream()
                .filter(model -> model.name().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<FleetYardsModel> search(String query, int limit)
    {
        final String lowerCaseQuery = query.toLowerCase();
        return this.modelsBySlug.values().stream()
                .filter(model -> model.name().toLowerCase().contains(lowerCaseQuery))
                .sorted(Comparator.comparing(FleetYardsModel::name))
                .limit(limit)
                .toList();
    }
}
