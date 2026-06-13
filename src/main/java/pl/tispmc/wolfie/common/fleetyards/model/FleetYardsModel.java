package pl.tispmc.wolfie.common.fleetyards.model;

public record FleetYardsModel(
        String slug,
        String name,
        Manufacturer manufacturer,
        String focus,
        String classificationLabel,
        Metrics metrics,
        Crew crew,
        Media media,
        Links links)
{
    public record Manufacturer(String name) {}

    public record Metrics(Double length, String lengthLabel) {}

    public record Crew(String maxLabel) {}

    public record Media(StoreImage storeImage) {}

    public record StoreImage(String url, String smallUrl) {}

    public record Links(String storeUrl) {}
}
