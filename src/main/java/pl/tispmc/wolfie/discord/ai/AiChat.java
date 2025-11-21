package pl.tispmc.wolfie.discord.ai;

import pl.tispmc.wolfie.discord.ai.exception.CouldNotGenerateAiResponse;
import pl.tispmc.wolfie.discord.ai.model.AiChatMessageRequest;
import pl.tispmc.wolfie.discord.ai.model.AiChatMessageResponse;

public interface AiChat
{
    AiChatMessageResponse sendMessage(AiChatMessageRequest params) throws CouldNotGenerateAiResponse;

    boolean isInitialized();

    void initialize();
}
