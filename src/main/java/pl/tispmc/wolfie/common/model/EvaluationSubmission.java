package pl.tispmc.wolfie.common.model;

import lombok.Data;
import pl.tispmc.wolfie.common.dto.SubmittedUser;

import java.util.ArrayList;
import java.util.List;

@Data
public class EvaluationSubmission
{
    private String missionName;
    private List<SubmittedUser> users = new ArrayList<>();
}
