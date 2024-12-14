package pl.tispmc.wolfie.common.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class User
{
    long id;
    String name;
    String avatarUrl;
}
