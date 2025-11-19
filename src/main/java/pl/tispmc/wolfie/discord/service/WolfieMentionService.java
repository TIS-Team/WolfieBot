package pl.tispmc.wolfie.discord.service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import pl.tispmc.wolfie.discord.config.GeminiConfig;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.MessageHistory;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class WolfieMentionService {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private final GeminiConfig geminiConfig;
    private final MessageCacheService messageCacheService;

    public void handleMention(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            log.debug("Ignoring message from bot: {}", event.getAuthor().getName());
            return;
        }

        boolean isMentioned = event.getMessage().getMentions().getUsers().stream()
                .anyMatch(user -> user.getId().equals(event.getJDA().getSelfUser().getId()));

        if (isMentioned) {
            log.info("Bot was mentioned by {}", event.getAuthor().getName());
            String question = event.getMessage().getContentRaw().replaceAll("<@!?" + event.getJDA().getSelfUser().getId() + ">", "").trim();
            log.info("Extracted question: '{}'", question);

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
                    final String[] longThinkingStates = {"Hmm... Jeszcze chwilkę...", "Hmm... Jeszcze chwilkę..", "Hmm... Jeszcze chwilkę."};
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
                        MessageHistory history = event.getChannel().getHistoryBefore(event.getMessageId(), 2).complete();
                        StringBuilder contextBuilder = new StringBuilder();
                        for (int i = history.getRetrievedHistory().size() - 1; i >= 0; i--) {
                            var message = history.getRetrievedHistory().get(i);
                            contextBuilder.append(message.getAuthor().getName()).append(": ").append(message.getContentRaw()).append("\n");
                        }
                        
                        String finalQuestion = contextBuilder.toString() + event.getAuthor().getName() + ": " + question;

                        String eventsInfo = formatScheduledEvents(event.getGuild().getScheduledEvents());
                        String fullPrompt = buildFullPrompt(finalQuestion, eventsInfo);

                        if (fullPrompt == null) {
                            if (currentAnimationTask[0] != null) currentAnimationTask[0].cancel(false);
                            longWaitTrigger.cancel(false);
                            thinkingMessage.editMessage("An error occurred while loading my knowledge base.").queue();
                            return;
                        }

                        try (VertexAI vertexAI = new VertexAI("gen-lang-client-0791168880", "europe-west1")) {
                            String modelName = isProgrammingQuestion(question) ? geminiConfig.getProModelName() : geminiConfig.getModelName();
                            log.info("Initializing VertexAI and GenerativeModel with model: {}", modelName);
                            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
                            GenerateContentResponse response = model.generateContent(fullPrompt);
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

                            messageCacheService.addMessage(event.getAuthor().getId(), "User: " + question);
                            messageCacheService.addMessage(event.getAuthor().getId(), "Bot: " + text);
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
        } else {
            log.trace("Bot was not mentioned in the message.");
        }
    }

    private String formatScheduledEvents(List<net.dv8tion.jda.api.entities.ScheduledEvent> events) {
        if (events == null || events.isEmpty()) {
            return "Aktualnie nie ma zaplanowanych żadnych wydarzeń.";
        }

        StringBuilder eventsInfo = new StringBuilder("Oto lista nadchodzących wydarzeń na serwerze:\n");
        for (net.dv8tion.jda.api.entities.ScheduledEvent event : events) {
            String formattedDate = event.getStartTime().format(dateFormatter);
            String formattedTime = event.getStartTime().format(timeFormatter);
            eventsInfo.append(String.format("- **%s**: %s o %s\n",
                    event.getName(),
                    formattedDate,
                    formattedTime));
        }
        return eventsInfo.toString();
    }

    private String buildFullPrompt(String question, String eventsInfo) {
        StringBuilder fullPromptBuilder = new StringBuilder();
        if (geminiConfig.getSystemPrompt() != null) {
            fullPromptBuilder.append(geminiConfig.getSystemPrompt()).append("\n\n");
        }

        fullPromptBuilder.append("### AKTUALNE WYDARZENIA NA SERWERZE:\n").append(eventsInfo).append("\n\n");

        if (geminiConfig.getKnowledgeBaseFile() != null && !geminiConfig.getKnowledgeBaseFile().isEmpty()) {
            try {
                Resource resource = new ClassPathResource(geminiConfig.getKnowledgeBaseFile());
                try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    String knowledgeBaseContent = FileCopyUtils.copyToString(reader);
                    fullPromptBuilder.append("### KONTEKST (BAZA WIEDZY):\n").append(knowledgeBaseContent).append("\n\n");
                    log.info("Loaded knowledge base from: {}", geminiConfig.getKnowledgeBaseFile());
                }
            } catch (IOException e) {
                log.error("Failed to load knowledge base file: {}", geminiConfig.getKnowledgeBaseFile(), e);
                return null;
            }
        }
        fullPromptBuilder.append(question);
        return fullPromptBuilder.toString();
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

    private boolean isProgrammingQuestion(String question) {
        List<String> keywords = Arrays.asList(
                "java", "python", "javascript", "code", "programming", "programowanie", "kod", "klasa", "funkcja",
                "metoda", "spring", "jpa", "hibernate", "sql", "docker", "maven", "gradle", "git", "github", "gitlab",
                "bitbucket", "intellij", "eclipse", "vscode", "visual studio", "debug", "test", "compiler",
                "kompilator", "error", "exception", "bug", "błąd", "algorytm", "algorithm", "rekurencja", "recursion",
                "pętla", "loop", "warunek", "if", "else", "switch", "case", "while", "for", "do", "foreach", "lambda",
                "stream", "api", "rest", "json", "xml", "html", "css", "js", "ts", "typescript", "angular", "react",
                "vue", "node", "npm", "yarn", "php", "c#", "c++", "assembler", "asembler", "asm", "pro"
        );

        String lowerCaseQuestion = " " + question.toLowerCase() + " "; // Add spaces for word boundary matching
        for (String keyword : keywords) {
            String pattern = "\\b" + keyword + "\\b"; // Match whole words
            if (java.util.regex.Pattern.compile(pattern).matcher(lowerCaseQuestion).find()) {
                log.info("Programming keyword '{}' found in question, switching to pro model.", keyword);
                return true;
            }
        }
        return false;
    }

    private String buildFullPrompt(String question) {
        StringBuilder fullPromptBuilder = new StringBuilder();
        if (geminiConfig.getSystemPrompt() != null) {
            fullPromptBuilder.append(geminiConfig.getSystemPrompt()).append("\n\n");
        }

        if (geminiConfig.getKnowledgeBaseFile() != null && !geminiConfig.getKnowledgeBaseFile().isEmpty()) {
            try {
                org.springframework.core.io.Resource resource = new ClassPathResource(geminiConfig.getKnowledgeBaseFile());
                try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    String knowledgeBaseContent = FileCopyUtils.copyToString(reader);
                    fullPromptBuilder.append("### KONTEKST (BAZA WIEDZY):\n").append(knowledgeBaseContent).append("\n\n");
                    log.info("Loaded knowledge base from: {}", geminiConfig.getKnowledgeBaseFile());
                }
            } catch (IOException e) {
                log.error("Failed to load knowledge base file: {}", geminiConfig.getKnowledgeBaseFile(), e);
                return null;
            }
        }
        fullPromptBuilder.append(question);
        return fullPromptBuilder.toString();
    }
}

