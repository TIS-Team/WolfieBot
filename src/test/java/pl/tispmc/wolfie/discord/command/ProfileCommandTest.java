package pl.tispmc.wolfie.discord.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.User;
import org.junit.jupiter.api.Test;
import pl.tispmc.wolfie.discord.service.UserStatsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ProfileCommandTest
{
    @Test
    void getAliasesShouldReturnProfile()
    {
        ProfileCommand profileCommand = new ProfileCommand(mock(UserStatsService.class));
        List<String> aliasList = profileCommand.getAliases();
        assertThat(aliasList).containsExactly("profil");
    }
}