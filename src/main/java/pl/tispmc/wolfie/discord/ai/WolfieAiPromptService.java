package pl.tispmc.wolfie.discord.ai;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import pl.tispmc.wolfie.WolfieApplication;
import pl.tispmc.wolfie.discord.ai.model.AiChatMessageRequest;
import pl.tispmc.wolfie.discord.ai.model.AiChatMessageResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.MessageHistory;
import pl.tispmc.wolfie.discord.config.AiConfig;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class WolfieAiPromptService
{
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private static final String BOT_NAME = "Wolfie";

    private final MessageCacheService messageCacheService;
    private final WolfiePersonalityService personalitySelector;

    private final AiConfig aiConfig;
    private final AiChat aiChat;

    private String systemPrompt;
    private String knowledge;

    @PostConstruct
    public void postConstruct() throws IOException
    {
        log.info("Loading system prompt and knowledge files...");
        this.systemPrompt = loadAiSystemPrompt();
        this.knowledge = loadAiKnowledge();
        log.info("Files loaded.");
    }

    public void handleMessage(MessageReceivedEvent event) {
        log.info("Bot was mentioned by {}", event.getAuthor().getName());
        InputChatMessage inputChatMessage = parseMessage(event);
        log.info("Extracted question: '{}' with attachments: {}", inputChatMessage.getText(), inputChatMessage.getAttachmentsAsString());

        event.getMessage().reply("Myślę...").queue(thinkingMessage -> {
            // Use a single-threaded scheduler for all animation and delayed tasks
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            // Atomic reference to the current animation task, allows reassignment
            final ScheduledFuture<?>[] currentAnimationTask = new ScheduledFuture[1];

            // Initial animation states
            final String[] thinkingStates = {"Myślę.", "Myślę..", "Myślę..."};
            AtomicInteger stateIndex = new AtomicInteger(0);

            // Start initial animation
            Runnable initialAnimation = () -> {
                int currentIndex = stateIndex.getAndIncrement() % thinkingStates.length;
                thinkingMessage.editMessage(thinkingStates[currentIndex]).queue();
            };
            currentAnimationTask[0] = scheduler.scheduleAtFixedRate(initialAnimation, 0, 1, TimeUnit.SECONDS);

            // Schedule long wait message trigger
            ScheduledFuture<?> longWaitTrigger = scheduler.schedule(() -> {
                // Cancel initial animation if still active
                if (currentAnimationTask[0] != null) {
                    currentAnimationTask[0].cancel(false);
                }

                // Start long-wait animation
                final String[] longThinkingStates = {"Hmm... Jeszcze chwilkę.", "Hmm... Jeszcze chwilkę..", "Hmm... Jeszcze chwilkę..."};
                stateIndex.set(0); // Reset index for new animation
                Runnable longWaitAnimation = () -> {
                    int currentIndex = stateIndex.getAndIncrement() % longThinkingStates.length;
                    thinkingMessage.editMessage(longThinkingStates[currentIndex]).queue();
                };
                currentAnimationTask[0] = scheduler.scheduleAtFixedRate(longWaitAnimation, 0, 1, TimeUnit.SECONDS);
            }, 7, TimeUnit.SECONDS);

            CompletableFuture.runAsync(() -> {
                try {
                    // Fetch last 2 messages for context
                    MessageHistory history = event.getChannel().getHistoryBefore(event.getMessageId(), 20).complete();
                    Deque<String> conversationHistory = Optional.ofNullable(messageCacheService.getHistory(inputChatMessage.getAuthorId())).orElse(new LinkedList<>());
                    String eventsInfo = formatScheduledEvents(event.getGuild().getScheduledEvents());

                    AiChatMessageRequest aiChatMessageRequest = buildChatMessageRequest(inputChatMessage, history, conversationHistory, inputChatMessage.getAttachments(), eventsInfo);
                    AiChatMessageResponse aiChatMessageResponse = aiChat.sendMessage(aiChatMessageRequest);
                    saveConversationCache(aiChatMessageRequest, aiChatMessageResponse);

                    if (currentAnimationTask[0] != null) currentAnimationTask[0].cancel(false);
                    longWaitTrigger.cancel(false);

                    List<String> messages = splitMessage(aiChatMessageResponse.getResponse());
                    thinkingMessage.delete().queue();
                    event.getMessage().reply(messages.get(0)).queue();
                    for (int i = 1; i < messages.size(); i++) {
                        event.getMessage().reply(messages.get(i)).queue(); // Reply to the original message for subsequent parts
                    }
                } catch (Exception e) {
                    log.error("An error occurred while communicating with Gemini API", e);
                    if (currentAnimationTask[0] != null) currentAnimationTask[0].cancel(false);
                    longWaitTrigger.cancel(false);
                    thinkingMessage.delete().queue(); // Delete the thinking message on error too
                    event.getMessage().reply("An error occurred while processing your request.").queue();
                } finally {
                    scheduler.shutdown(); // Ensure the scheduler is always shut down
                }
            });
        });
    }

    private void saveConversationCache(AiChatMessageRequest aiChatMessageRequest, AiChatMessageResponse aiChatMessageResponse)
    {
        messageCacheService.addMessage(aiChatMessageRequest.getAuthorId(), aiChatMessageRequest.getAuthorId() + ": " + aiChatMessageRequest.getOriginalQuestion());
        messageCacheService.addMessage(aiChatMessageRequest.getAuthorId(), aiChatMessageRequest.getBotName() + ": " + aiChatMessageResponse.getResponse());
    }

    private static InputChatMessage parseMessage(MessageReceivedEvent event)
    {
        String question = event.getMessage().getContentRaw()
                .replace(event.getJDA().getSelfUser().getAsMention(), BOT_NAME)
                .trim();

        question = replaceMentionsInDiscordMessage(question, event.getMessage().getMentions());

        List<InputChatMessage.Attachment> attachments = event.getMessage().getAttachments()
                .stream()
                .filter(Message.Attachment::isImage)
                .map(attachment -> new InputChatMessage.Attachment(attachment.getProxyUrl(), attachment.getContentType()))
                .toList();

        return new InputChatMessage(event.getMessage().getAuthor().getId(), event.getMessage().getAuthor().getName(), question, attachments);
    }

    private static String replaceMentionsInDiscordMessage(String message, Mentions mentions)
    {
        for (Member mentionedMember : mentions.getMembers())
        {
            message = message.replace(mentionedMember.getAsMention(), mentionedMember.getEffectiveName());
        }
        return message;
    }

    private static String formatScheduledEvents(List<ScheduledEvent> events) {
        if (events == null || events.isEmpty()) {
            return "Aktualnie nie ma zaplanowanych żadnych wydarzeń.";
        }

        StringBuilder eventsInfo = new StringBuilder("Oto lista nadchodzących wydarzeń na serwerze:\n");
        for (ScheduledEvent event : events) {
            String formattedDate = event.getStartTime().format(dateFormatter);
            String formattedTime = event.getStartTime().format(timeFormatter);
            eventsInfo.append(String.format("- **%s**: %s o %s\n",
                    event.getName(),
                    formattedDate,
                    formattedTime));
        }
        return eventsInfo.toString();
    }

    private AiChatMessageRequest buildChatMessageRequest(InputChatMessage inputChatMessage,
                                                         MessageHistory discordMessageHistory,
                                                         Deque<String> authorConversationHistory,
                                                         List<InputChatMessage.Attachment> attachments,
                                                         String eventsInfo) {
        List<String> parts = new ArrayList<>();

        // System Prompt
        parts.add("### INFORMACJE BAZOWE (NAJWAŻNIEJSZE) ### \n" + this.systemPrompt);

        // Personality
        parts.add("### NASTRÓJ / HUMOR ### \n" + loadPersonality());

        // Knowledge
        parts.add("### KONTEKST (BAZA WIEDZY) ### \n" + this.knowledge);

        // Server events
        parts.add("### AKTUALNE WYDARZENIA NA SERWERZE ### \n" + eventsInfo);

        // Chat History
        StringBuilder historyPartBuilder = new StringBuilder();
        historyPartBuilder.append("### HISTORIA CZATU ###").append("\n");
        for (int i = discordMessageHistory.getRetrievedHistory().size() - 1; i >= 0; i--) {
            Message message = discordMessageHistory.getRetrievedHistory().get(i);
            historyPartBuilder.append(message.getAuthor().getEffectiveName())
                    .append(": ")
                    .append(replaceMentionsInDiscordMessage(message.getContentRaw(), message.getMentions()))
                    .append("\n");
        }

        parts.add(historyPartBuilder.toString());

        // Conversation history with given user
        if (!authorConversationHistory.isEmpty()) {
            StringBuilder conversationContext = new StringBuilder();
            conversationContext.append("### KONTEKST ROZMOWY ###").append("\n");
            authorConversationHistory.forEach(message -> conversationContext.append(message).append("\n"));
            parts.add(conversationContext.toString());
        }

        // Actual message
        parts.add("### AKTUALNA WIADOMOŚĆ: \n" + inputChatMessage.getAuthor() + ": " + inputChatMessage.getText());

        // Attachments
        List<AiChatMessageRequest.Attachment> messageAttachments = attachments.stream()
                .map(attachment -> AiChatMessageRequest.Attachment.builder()
                        .url(attachment.getUrl())
                        .mimeType(attachment.getMimeType())
                        .build())
                .toList();

        return AiChatMessageRequest.builder()
                .botName(BOT_NAME)
                .authorId(inputChatMessage.getAuthor())
                .originalQuestion(inputChatMessage.getText())
                .parts(parts)
                .attachments(messageAttachments)
                .build();
    }

    private String loadPersonality()
    {
        return this.personalitySelector.getWolfiePersonality();
    }

    private static List<String> splitMessage(String longMessage) {
        List<String> parts = new ArrayList<>();
        if (longMessage.length() <= 2000) {
            parts.add(longMessage);
            return parts;
        }

        String[] lines = longMessage.split("\n");
        StringBuilder currentPart = new StringBuilder();
        boolean inCodeBlock = false;
        String codeBlockLang = "";

        for (String line : lines) {
            if (line.startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                if (inCodeBlock) {
                    codeBlockLang = line.substring(3);
                }
            }

            if (currentPart.length() + line.length() + 1 > 1990) { // 1990 to be safe
                if (inCodeBlock) {
                    currentPart.append("\n```");
                }
                parts.add(currentPart.toString());
                currentPart = new StringBuilder();
                if (inCodeBlock) {
                    currentPart.append("```").append(codeBlockLang).append("\n");
                }
            }
            currentPart.append(line).append("\n");
        }

        parts.add(currentPart.toString());
        return parts;
    }

    private String loadAiSystemPrompt() throws IOException
    {
        Resource resource = new ClassPathResource(aiConfig.getSystemPromptFile(), WolfieApplication.class.getClassLoader());
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }

    private String loadAiKnowledge() throws IOException
    {
        Resource resource = new ClassPathResource(aiConfig.getKnowledgeBaseFile(), WolfieApplication.class.getClassLoader());
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Value
    private static class InputChatMessage
    {
        String authorId;
        String author;
        String text;
        List<Attachment> attachments;

        public String getAttachmentsAsString()
        {
            return Arrays.toString(attachments.stream().map(Attachment::getUrl).toArray());
        }

        @Value
        private static class Attachment
        {
            String url;
            String mimeType;
        }
    }
}

