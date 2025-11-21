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
    List<String> parts;
    String systemInstruction;
    String authorId;
    String botName;
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
