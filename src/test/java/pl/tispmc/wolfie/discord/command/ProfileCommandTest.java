package pl.tispmc.wolfie.discord.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.tispmc.wolfie.common.service.UserDataService;
import pl.tispmc.wolfie.common.util.DateTimeProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProfileCommandTest
{
    @Mock
    private UserDataService userDataService;
    @Mock
    private DateTimeProvider dateTimeProvider;

    private ProfileCommand profileCommand;

    @BeforeEach
    void setUp()
    {
        profileCommand = new ProfileCommand("123", userDataService, dateTimeProvider);
    }

    @Test
    void getAliasesShouldReturnProfile()
    {
        List<String> aliasList = profileCommand.getAliases();
        assertThat(aliasList).containsExactly("profil");
    }
}