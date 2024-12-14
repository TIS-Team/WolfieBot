package pl.tispmc.wolfie.common.model;

import lombok.Value;

@Value(staticConstructor = "of")
public class UserId
{
    long id;
}
