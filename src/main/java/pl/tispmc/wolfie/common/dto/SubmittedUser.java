package pl.tispmc.wolfie.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pl.tispmc.wolfie.common.model.Action;

import java.util.EnumSet;

@Data
public class SubmittedUser
{
    @JsonProperty("user_id")
    private Long userId;
    private EnumSet<Action> actions;
}
