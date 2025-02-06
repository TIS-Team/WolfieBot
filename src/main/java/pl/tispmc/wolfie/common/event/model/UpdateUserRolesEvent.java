package pl.tispmc.wolfie.common.event.model;

import org.springframework.context.ApplicationEvent;

import java.util.Set;

public class UpdateUserRolesEvent extends ApplicationEvent
{
    private final Set<Long> userIdsToUpdate;

    public UpdateUserRolesEvent(Object source, Set<Long> userIdsToUpdate)
    {
        super(source);
        this.userIdsToUpdate = userIdsToUpdate;
    }

    public Set<Long> getUsersIdsToUpdate()
    {
        return userIdsToUpdate;
    }
}
