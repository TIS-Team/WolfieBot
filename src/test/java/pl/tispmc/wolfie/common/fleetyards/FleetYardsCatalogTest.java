package pl.tispmc.wolfie.common.fleetyards;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.tispmc.wolfie.common.fleetyards.model.FleetYardsModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FleetYardsCatalogTest
{
    @Mock
    private FleetYardsClient fleetYardsClient;

    private FleetYardsCatalog fleetYardsCatalog;

    @BeforeEach
    void setUp()
    {
        fleetYardsCatalog = new FleetYardsCatalog(fleetYardsClient);
    }

    @Test
    void shouldBeEmptyBeforeFirstRefresh()
    {
        assertThat(fleetYardsCatalog.isEmpty()).isTrue();
    }

    @Test
    void findBySlugShouldReturnModelAfterRefresh()
    {
        // given
        given(fleetYardsClient.fetchAllModels()).willReturn(List.of(shipModel("drak-cutlass-black", "Cutlass Black")));

        // when
        fleetYardsCatalog.refresh();

        // then
        assertThat(fleetYardsCatalog.findBySlug("drak-cutlass-black"))
                .hasValueSatisfying(model -> assertThat(model.name()).isEqualTo("Cutlass Black"));
        assertThat(fleetYardsCatalog.findBySlug("aegs-gladius")).isEmpty();
    }

    @Test
    void findByNameShouldIgnoreCase()
    {
        // given
        given(fleetYardsClient.fetchAllModels()).willReturn(List.of(shipModel("drak-cutlass-black", "Cutlass Black")));

        // when
        fleetYardsCatalog.refresh();

        // then
        assertThat(fleetYardsCatalog.findByName("cutlass BLACK")).isPresent();
    }

    @Test
    void searchShouldReturnMatchingModelsSortedByNameAndLimited()
    {
        // given
        given(fleetYardsClient.fetchAllModels()).willReturn(List.of(
                shipModel("drak-cutlass-red", "Cutlass Red"),
                shipModel("drak-cutlass-black", "Cutlass Black"),
                shipModel("aegs-gladius", "Gladius")));

        // when
        fleetYardsCatalog.refresh();

        // then
        List<FleetYardsModel> foundModels = fleetYardsCatalog.search("cutlass", 25);
        assertThat(foundModels)
                .extracting(FleetYardsModel::name)
                .containsExactly("Cutlass Black", "Cutlass Red");

        assertThat(fleetYardsCatalog.search("cutlass", 1)).hasSize(1);
    }

    @Test
    void refreshShouldKeepOldCatalogWhenClientFails()
    {
        // given
        given(fleetYardsClient.fetchAllModels())
                .willReturn(List.of(shipModel("drak-cutlass-black", "Cutlass Black")))
                .willThrow(new RuntimeException("FleetYards is down"));

        // when
        fleetYardsCatalog.refresh();
        fleetYardsCatalog.refresh();

        // then
        assertThat(fleetYardsCatalog.findBySlug("drak-cutlass-black")).isPresent();
    }

    private FleetYardsModel shipModel(String slug, String name)
    {
        return new FleetYardsModel(slug, name, null, null, null, null, null, null, null);
    }
}
