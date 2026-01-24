package pl.tispmc.wolfie.discord.ai.model;

import lombok.Builder;
import lombok.Value;

import java.util.Arrays;
import java.util.List;

@Builder
@Value
public class AiChatMessageRequest
{
    String originalQuestion;
    String authorId;
    String botName;

    // Message parts
    String preparedQuestion;
    String personality;
    String knowledgeBase;
    String baseSystemInstruction; // Base system instruction
    String preparedFullSystemInstruction; // Prepared full system instruction. Includes baseSystemInstruction + personality + knowledgeBase + message history and more. Should be used most of the time
    List<Attachment> attachments;

    public String getAttachmentsAsString()
    {
        return Arrays.toString(attachments.stream().map(Attachment::getUrl).toArray());
    }

    @Value
    @Builder
    public static class Attachment
    {
        String url;
        String mimeType;
    }
}
