package pl.tispmc.wolfie.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.EnumSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserExpChange
{
    private long userId;
    private EnumSet<Action> actions;
    private int calculatedExpChange;
}
