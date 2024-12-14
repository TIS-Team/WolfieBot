package pl.tispmc.wolfie.discord.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.tispmc.wolfie.common.service.UserDataService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProfileCommandTest
{
    @Mock
    private UserDataService userDataService;

    @InjectMocks
    private ProfileCommand profileCommand;

    @Test
    void getAliasesShouldReturnProfile()
    {
        List<String> aliasList = profileCommand.getAliases();
        assertThat(aliasList).containsExactly("profil");
    }
}