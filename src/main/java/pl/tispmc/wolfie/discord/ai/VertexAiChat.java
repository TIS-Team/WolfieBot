package pl.tispmc.wolfie.discord.ai;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FileData;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.ai.exception.CouldNotGenerateAiResponse;
import pl.tispmc.wolfie.discord.ai.model.AiChatMessageRequest;
import pl.tispmc.wolfie.discord.ai.model.AiChatMessageResponse;
import pl.tispmc.wolfie.discord.config.GeminiConfig;
import pl.tispmc.wolfie.discord.service.MessageCacheService;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Component
public class VertexAiChat implements AiChat
{
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

    private VertexAI vertexAI;

    @Override
    public AiChatMessageResponse sendMessage(AiChatMessageRequest params) throws CouldNotGenerateAiResponse
    {
        if (!isInitialized())
            initialize();

        String aiModel = selectAiModel(params.getOriginalQuestion());
        log.info("Selected AI Model: {}", aiModel);

        GenerativeModel model = new GenerativeModel(aiModel, vertexAI);
        Content content = buildPromptMessage(params);

        log.info("Final AI prompt: '{}'", content.toString());

        GenerateContentResponse response = null;
        try
        {
            response = model.generateContent(content);
        }
        catch (IOException e)
        {
            throw new CouldNotGenerateAiResponse("Could not generate AI response", e);
        }

        String text = ResponseHandler.getText(response);
        log.info("Generated response from Gemini: '{}'", text);

        messageCacheService.addMessage(params.getAuthorId(), params.getAuthorId() + ": " + params.getOriginalQuestion());
        messageCacheService.addMessage(params.getAuthorId(), params.getBotName() + ": " + text);

        return AiChatMessageResponse.builder().response(text).build();
    }

    @Override
    public boolean isInitialized()
    {
        return vertexAI != null && !vertexAI.getPredictionServiceClient().isShutdown();
    }

    @Override
    public void initialize()
    {
        this.vertexAI = new VertexAI.Builder()
                .setProjectId(this.geminiConfig.getProjectId())
                .setLocation(this.geminiConfig.getLocation())
                .build();
    }

    private static Content buildPromptMessage(AiChatMessageRequest params)
    {
        Content.Builder contentBuilder = Content.newBuilder();

        for (String part : params.getParts())
        {
            contentBuilder.addParts(Part.newBuilder()
                    .setText(part)
                    .build());
        }

        for (AiChatMessageRequest.Attachment attachment : params.getAttachments())
        {
            contentBuilder.addParts(Part.newBuilder()
                    .setFileData(FileData.newBuilder()
                            .setFileUri(attachment.getUrl())
                            .setMimeType(attachment.getMimeType())
                            .build())
                    .build());
        }

        return contentBuilder
                .setRole("user")
                .build();
    }

    private String selectAiModel(String originalQuestion)
    {
        return shouldUseProModel(originalQuestion) ? geminiConfig.getProModelName() : geminiConfig.getModelName();
    }

    private static boolean shouldUseProModel(String question) {
        String lowerCaseQuestion = " " + question.toLowerCase() + " "; // Add spaces for word boundary matching
        for (String keyword : PRO_MODEL_KEYS) {
            String pattern = "\\b" + keyword + "\\b"; // Match whole words
            if (Pattern.compile(pattern).matcher(lowerCaseQuestion).find()) {
                log.info("PRO MODEL keyword '{}' found in question, switching to pro model.", keyword);
                return true;
            }
        }
        return false;
    }
}
