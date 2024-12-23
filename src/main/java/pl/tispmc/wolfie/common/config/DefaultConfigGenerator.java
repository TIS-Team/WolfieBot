package pl.tispmc.wolfie.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
@Component
public class DefaultConfigGenerator implements CommandLineRunner
{
    @Override
    public void run(String... args) throws Exception
    {
        Path configFilePath = Paths.get(".").resolve("config").resolve("application.properties");

        if (Files.exists(configFilePath))
            return;
        log.info("Creating default config file in: {}", configFilePath);
        Files.createDirectories(configFilePath.getParent());
        Files.write(configFilePath, new ClassPathResource("default-application.properties").getContentAsByteArray(), StandardOpenOption.CREATE);
    }
}
