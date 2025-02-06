package pl.tispmc.wolfie.discord.service;

import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.model.UserExpChangeDiscordMessageParams;

import java.util.List;

@Component
public class DiscordExpChangeMessagePublisher
{
    public void publishMessage(List<UserExpChangeDiscordMessageParams> params)
    {
        //TODO: Publish exp change messages

    }
}
