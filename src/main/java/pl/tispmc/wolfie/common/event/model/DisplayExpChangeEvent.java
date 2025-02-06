package pl.tispmc.wolfie.common.event.model;

import org.springframework.context.ApplicationEvent;
import pl.tispmc.wolfie.common.model.UserExpChangeDiscordMessageParams;

import java.util.List;

public class DisplayExpChangeEvent extends ApplicationEvent
{
    private final List<UserExpChangeDiscordMessageParams> params;

    public DisplayExpChangeEvent(Object source, List<UserExpChangeDiscordMessageParams> params)
    {
        super(source);
        this.params = params;
    }

    public List<UserExpChangeDiscordMessageParams> getParams()
    {
        return params;
    }
}
