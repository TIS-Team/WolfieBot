package pl.tispmc.wolfie.discord.ai.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AiChatMessageResponse
{
    String response;
}
