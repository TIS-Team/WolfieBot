package pl.tispmc.wolfie.discord.service.ai;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FileData;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
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
import pl.tispmc.wolfie.discord.config.GeminiConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.MessageHistory;
import pl.tispmc.wolfie.discord.service.MessageCacheService;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class WolfieAiPromptService
{
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private static final Set<String> PRO_MODEL_KEYS = Set.of(
            // --- BAZOWE WYMAGANE (Twoje prośby) ---
            "stm32", "program", "programuj", "zaprogramuj", "oprogramowanie", "koduj", "pro",

            // --- SKRYPTY I AUTOMATYZACJA (To o co pytałeś) ---
            "skrypt", "skrypty", "skryptu", "skryptowanie", "script", "scripting",
            "makro", "makra", "macro", // często mylone z kodem
            "batch", "bash", "powershell", "shell", "cmd", "terminal", "konsola",
            "wiersz poleceń", "command line", "argumenty", "parametry startowe",

            // --- HARDWARE / EMBEDDED (Specyficzne dla STM i elektroniki) ---
            "mikrokontroler", "microcontroller", "arduino", "esp32", "esp8266", "raspberry",
            "gpio", "uart", "i2c", "spi", "pwm", "adc", "dac", "watchdog", "timer",
            "rejestr", "register", "przerwanie", "interrupt", "bootloader", "wsad",
            "flashowanie", "flashing", "układ scalony", "płytka stykowa", "breadboard",

            // --- PLIKI I ROZSZERZENIA (Bardzo bezpieczne trigger-y) ---
            ".py", ".js", ".java", ".cpp", ".h", ".c", ".cs", ".php", ".sh", ".bat", ".json", ".xml", ".bin", ".hex",

            // --- NARZĘDZIA I ŚRODOWISKA ---
            "cubeide", "keil", "hal", "cmsis", "platformio", "make", "cmake", "gcc", "g++",
            "maven", "gradle", "docker", "kubernetes", "git", "repo", "branch", "merge",
            "visual studio", "vscode", "intellij", "pycharm", "eclipse",

            // --- JĘZYKI (Tylko techniczne nazwy) ---
            "python", "javascript", "typescript", "c++", "c#", "csharp", "rust", "golang",
            "kotlin", "swift", "scala", "perl", "ruby", "lua", "assembler", "asm", "sql", "nosql",

            // --- POJĘCIA PROGRAMISTYCZNE (Bełkot techniczny) ---
            "kompilacja", "kompilator", "compiler", "linker", "build",
            "debug", "debugger", "breakpoint", "stack trace", "zrzut pamięci",
            "wyjątek", "exception", "segfault", "nullpointer", "błąd składni", "syntax error",
            "zmienna środowiskowa", "biblioteka", "library", "framework", "moduł",
            "refaktoryzacja", "deploy", "wdrożenie", "backend", "frontend", "api", "rest", "endpoint"
    );

    private final GeminiConfig geminiConfig;
    private final MessageCacheService messageCacheService;
    private final WolfiePersonalityService personalitySelector;

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
        PromptMessage promptMessage = parseMessage(event);
        log.info("Extracted question: '{}' with attachments: {}", promptMessage.getText(), promptMessage.getAttachmentsAsString());

        event.getMessage().reply("Wolfie myśli...").queue(thinkingMessage -> {
            // Use a single-threaded scheduler for all animation and delayed tasks
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            // Atomic reference to the current animation task, allows reassignment
            final ScheduledFuture<?>[] currentAnimationTask = new ScheduledFuture[1];

            // Initial animation states
            final String[] thinkingStates = {"Wolfie myśli", "Wolfie myśli.", "Wolfie myśli..", "Wolfie myśli..."};
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
                    MessageHistory history = event.getChannel().getHistoryBefore(event.getMessageId(), 6).complete();
                    Deque<String> conversationHistory = Optional.ofNullable(messageCacheService.getHistory(promptMessage.getAuthorId())).orElse(new LinkedList<>());
                    String eventsInfo = formatScheduledEvents(event.getGuild().getScheduledEvents());

                    Content promptContent = buildFullPrompt(promptMessage, history, conversationHistory, promptMessage.getAttachments(), eventsInfo);
                    log.info("Final AI prompt: '{}'", promptContent.toString());

                    try (VertexAI vertexAI = new VertexAI("gen-lang-client-0791168880", "europe-west1")) {
                        String modelName = shouldUseProModel(promptMessage.getText()) ? geminiConfig.getProModelName() : geminiConfig.getModelName();
                        log.info("Initializing VertexAI and GenerativeModel with model: {}", modelName);
                        GenerativeModel model = new GenerativeModel(modelName, vertexAI);
                        GenerateContentResponse response = model.generateContent(promptContent);
                        String text = ResponseHandler.getText(response);
                        log.info("Generated response from Gemini: '{}'", text);

                        if (currentAnimationTask[0] != null) currentAnimationTask[0].cancel(false);
                        longWaitTrigger.cancel(false);

                        List<String> messages = splitMessage(text);
                        thinkingMessage.delete().queue();
                        event.getMessage().reply(messages.get(0)).queue();
                        for (int i = 1; i < messages.size(); i++) {
                            event.getMessage().reply(messages.get(i)).queue(); // Reply to the original message for subsequent parts
                        }

                        messageCacheService.addMessage(promptMessage.getAuthorId(), promptMessage.getAuthor() + ": " + promptMessage.getText());
                        messageCacheService.addMessage(promptMessage.getAuthorId(), event.getJDA().getSelfUser().getEffectiveName() + ": " + text);
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

    private PromptMessage parseMessage(MessageReceivedEvent event)
    {
        String question = event.getMessage().getContentRaw()
                .replace(event.getJDA().getSelfUser().getAsMention(), "Wolfie")
                .trim();

        question = replaceMentionsInDiscordMessage(question, event.getMessage().getMentions());

        List<PromptMessage.Attachment> attachments = event.getMessage().getAttachments()
                .stream()
                .filter(Message.Attachment::isImage)
                .map(attachment -> new PromptMessage.Attachment(attachment.getProxyUrl(), attachment.getContentType()))
                .toList();

        return new PromptMessage(event.getMessage().getAuthor().getId(), event.getMessage().getAuthor().getName(), question, attachments);
    }

    private String replaceMentionsInDiscordMessage(String message, Mentions mentions)
    {
        for (Member mentionedMember : mentions.getMembers())
        {
            message = message.replace(mentionedMember.getAsMention(), mentionedMember.getEffectiveName());
        }
        return message;
    }

    private String formatScheduledEvents(List<ScheduledEvent> events) {
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

    private Content buildFullPrompt(PromptMessage promptMessage,
                                    MessageHistory discordMessageHistory,
                                    Deque<String> authorConversationHistory,
                                    List<PromptMessage.Attachment> attachments,
                                    String eventsInfo) {
        Content.Builder contentBuilder = Content.newBuilder();

        // System Prompt
        contentBuilder.addParts(Part.newBuilder()
                .setText(this.systemPrompt)
                .build());

        // Personality
        contentBuilder.addParts(Part.newBuilder()
                .setText("--- OSOBOWOŚĆ I ZACHOWANIE --- \n" + loadPersonality())
                .build());

        // Knowledge
        contentBuilder.addParts(Part.newBuilder()
                .setText("### KONTEKST (BAZA WIEDZY):\n" + this.knowledge)
                .build());

        // Server events
        contentBuilder.addParts(Part.newBuilder()
                .setText("### AKTUALNE WYDARZENIA NA SERWERZE:\n" + eventsInfo)
                .build());

        // Chat History
        StringBuilder historyPartBuilder = new StringBuilder();
        historyPartBuilder.append("### HISTORIA CZATU:").append("\n");
        for (int i = discordMessageHistory.getRetrievedHistory().size() - 1; i >= 0; i--) {
            Message message = discordMessageHistory.getRetrievedHistory().get(i);
            historyPartBuilder.append(message.getAuthor().getEffectiveName())
                    .append(": ")
                    .append(replaceMentionsInDiscordMessage(message.getContentRaw(), message.getMentions()))
                    .append("\n");
        }

        contentBuilder.addParts(Part.newBuilder()
                .setText(historyPartBuilder.toString())
                .build());

        // Conversation history with given user
        StringBuilder conversationContext = new StringBuilder();
        if (!authorConversationHistory.isEmpty()) {
            conversationContext.append("### KONTEKST ROZMOWY:").append("\n");
            authorConversationHistory.forEach(message -> conversationContext.append(message).append("\n"));
        }

        contentBuilder.addParts(Part.newBuilder()
                    .setText(conversationContext.toString())
                .build());

        if (!attachments.isEmpty())
        {
            for (PromptMessage.Attachment attachment : attachments)
            {
                contentBuilder.addParts(Part.newBuilder()
                        .setFileData(FileData.newBuilder()
                                .setFileUri(attachment.getUrl())
                                .setMimeType(attachment.getMimeType())
                                .build())
                        .build());
            }
        }

        contentBuilder.addParts(Part.newBuilder()
                .setText("### AKTUALNA WIADOMOŚĆ: \n" + promptMessage.getAuthor() + ": " + promptMessage.getText())
                .build());

        return contentBuilder
                .setRole("user")
                .build();
    }

    private String loadPersonality()
    {
        return this.personalitySelector.getWolfiePersonality();
    }

    private List<String> splitMessage(String longMessage) {
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

    private boolean shouldUseProModel(String question) {
        String lowerCaseQuestion = " " + question.toLowerCase() + " "; // Add spaces for word boundary matching
        for (String keyword : PRO_MODEL_KEYS) {
            String pattern = "\\b" + keyword + "\\b"; // Match whole words
            if (Pattern.compile(pattern).matcher(lowerCaseQuestion).find()) {
                log.info("Programming keyword '{}' found in question, switching to pro model.", keyword);
                return true;
            }
        }
        return false;
    }

    private String loadAiSystemPrompt() throws IOException
    {
        Resource resource = new ClassPathResource(geminiConfig.getSystemPromptFile(), WolfieApplication.class.getClassLoader());
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }

    private String loadAiKnowledge() throws IOException
    {
        Resource resource = new ClassPathResource(geminiConfig.getKnowledgeBaseFile(), WolfieApplication.class.getClassLoader());
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Value
    private static class PromptMessage
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

