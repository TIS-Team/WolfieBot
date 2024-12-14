package pl.tispmc.wolfie.common.model;

import lombok.Value;

import java.util.UUID;

@Value(staticConstructor = "of")
public class EvaluationId
{
    private UUID id;
}
