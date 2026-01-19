package pl.tispmc.wolfie.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.tispmc.wolfie.common.dto.SubmittedUser;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationSubmission
{
    private String missionName;
    private List<SubmittedUser> users = new ArrayList<>();
}
