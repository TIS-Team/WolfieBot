package pl.tispmc.wolfie.common.model;

import lombok.Data;
import pl.tispmc.wolfie.common.dto.SubmittedUser;

import java.util.List;

@Data
public class EvaluationSubmission
{
    private List<SubmittedUser> users;
}
