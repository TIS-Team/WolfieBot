package pl.tispmc.wolfie.discord.application.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.event.model.UpdateUserRolesEvent;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class UpdateUserRolesListener
{
    private final AtomicBoolean alreadyInProgress = new AtomicBoolean(false);

    @Async
    @EventListener
    public void onUpdateUserRoles(UpdateUserRolesEvent event)
    {
        log.info("Got update user roles event.");
        if (alreadyInProgress.get()) {
            log.info("User roles update already in progress. Skipping additional execution.");
            return;
        }

        updateUserRoles();
    }

    private void updateUserRoles()
    {
        log.info("Updating user roles...");
    }
}
