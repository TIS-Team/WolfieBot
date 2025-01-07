package pl.tispmc.wolfie.discord.application.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.event.model.UpdateUserRolesEvent;
import pl.tispmc.wolfie.discord.service.DiscordUserRolesUpdater;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateUserRolesListener
{
    private static final AtomicBoolean UPDATE_IN_PROGRESS = new AtomicBoolean(false);
    private final DiscordUserRolesUpdater userRolesUpdater;

    @Async
    @EventListener
    public void onUpdateUserRoles(UpdateUserRolesEvent event)
    {
        log.info("Got update user roles event.");
        if (UPDATE_IN_PROGRESS.get()) {
            log.info("User roles update already in progress. Skipping additional execution.");
            return;
        }

        this.userRolesUpdater.scheduleRolesUpdate(event.getUsersIdsToUpdate());
        UPDATE_IN_PROGRESS.set(false);
    }
}
