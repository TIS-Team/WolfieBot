package pl.tispmc.wolfie.common.event.model;

import org.springframework.context.ApplicationEvent;

import java.util.List;

public class UpdateUserRolesEvent extends ApplicationEvent
{
    private final List<Long> userIdsToUpdate;

    public UpdateUserRolesEvent(Object source, List<Long> userIdsToUpdate)
    {
        super(source);
        this.userIdsToUpdate = userIdsToUpdate;
    }

    public List<Long> getUsersIdsToUpdate()
    {
        return userIdsToUpdate;
    }
}
