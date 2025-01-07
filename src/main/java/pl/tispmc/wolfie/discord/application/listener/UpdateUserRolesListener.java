package pl.tispmc.wolfie.discord.application.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.event.model.UpdateUserRolesEvent;
import pl.tispmc.wolfie.discord.service.DiscordUserRolesUpdater;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateUserRolesListener
{
    private final DiscordUserRolesUpdater userRolesUpdater;

    @Async
    @EventListener
    public void onUpdateUserRoles(UpdateUserRolesEvent event)
    {
        log.info("Scheduling user roles update for: {}", event.getUsersIdsToUpdate());
        this.userRolesUpdater.scheduleRolesUpdate(event.getUsersIdsToUpdate());
    }
}
