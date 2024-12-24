package pl.tispmc.wolfie.common.event.model;

import org.springframework.context.ApplicationEvent;

public class UpdateUserRolesEvent extends ApplicationEvent
{
    public UpdateUserRolesEvent(Object source)
    {
        super(source);
    }
}
