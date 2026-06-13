package pl.tispmc.wolfie.common.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
@Value
@Data
@AllArgsConstructor
public class UserShips
{
    long userId;
    String name;

    @Builder.Default
    List<String> ships = new ArrayList<>();
}
