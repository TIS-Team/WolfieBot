package pl.tispmc.wolfie.discord.ai;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.discord.ai.exception.CouldNotGenerateAiResponse;
import pl.tispmc.wolfie.discord.ai.model.AiChatMessageRequest;
import pl.tispmc.wolfie.discord.ai.model.AiChatMessageResponse;
import pl.tispmc.wolfie.discord.config.GeminiConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Component
public class GeminiAiChat implements AiChat
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

    private Client client;

    @Override
    public AiChatMessageResponse sendMessage(AiChatMessageRequest params) throws CouldNotGenerateAiResponse
    {
        if (!isInitialized())
            initialize();

        String aiModel = selectAiModel(params.getOriginalQuestion());
        log.info("Selected AI Model: {}", aiModel);

        Content content = buildPromptMessage(params);

        log.info("Final AI prompt: '{}'", content.toString());

        GenerateContentResponse response = null;
        try
        {
            response = client.models.generateContent(aiModel, content, null);
        }
        catch (Exception e)
        {
            throw new CouldNotGenerateAiResponse("Could not generate AI response", e);
        }

        String responseText = response.text();
        log.info("Generated response from Gemini: '{}'", responseText);

        return AiChatMessageResponse.builder().response(responseText).build();
    }

    @Override
    public boolean isInitialized()
    {
        return client != null;
    }

    @Override
    public void initialize()
    {
        this.client = Client.builder()
                .project(this.geminiConfig.getProjectId())
                .location(this.geminiConfig.getLocation())
                .vertexAI(true)
                .build();
    }

    private static Content buildPromptMessage(AiChatMessageRequest params)
    {
        List<Part> partList = new ArrayList<>();
        for (String part : params.getParts())
        {
            partList.add(Part.fromText(part));
        }

        for (AiChatMessageRequest.Attachment attachment : params.getAttachments())
        {
            partList.add(Part.fromUri(attachment.getUrl(), attachment.getMimeType()));
        }

        return Content.builder()
                .parts(partList)
                .role("user")
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
