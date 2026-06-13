package pl.tispmc.wolfie.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tispmc.wolfie.common.fleetyards.FleetYardsCatalog;
import pl.tispmc.wolfie.common.fleetyards.model.FleetYardsModel;
import pl.tispmc.wolfie.common.model.UserShips;
import pl.tispmc.wolfie.common.service.FleetService;
import pl.tispmc.wolfie.common.util.DateTimeProvider;
import pl.tispmc.wolfie.discord.command.exception.CommandException;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Component
public class HangarCommand extends AbstractSlashCommand
{
    private static final String SHIP_PARAM = "statek";
    private static final String USER_PARAM = "użytkownik";

    private static final String SUBCOMMAND_ADD = "dodaj";
    private static final String SUBCOMMAND_REMOVE = "usun";
    private static final String SUBCOMMAND_CLEAR = "wyczysc";
    private static final String SUBCOMMAND_LIST = "lista";

    private static final int MAX_AUTOCOMPLETE_CHOICES = 25;

    private final String adminRoleId;
    private final FleetService fleetService;
    private final FleetYardsCatalog fleetYardsCatalog;
    private final DateTimeProvider dateTimeProvider;

    public HangarCommand(
            @Value("${bot.channels.star-citizen.id:0}") String supportedChannelId,
            @Value("${bot.roles.tis-vulture.id:0}") String tisVultureRoleId,
            @Value("${bot.roles.admin.id:0}") String adminRoleId,
            FleetService fleetService,
            FleetYardsCatalog fleetYardsCatalog,
            DateTimeProvider dateTimeProvider)
    {
        super(Set.of(supportedChannelId), Set.of(tisVultureRoleId));
        this.adminRoleId = adminRoleId;
        this.fleetService = fleetService;
        this.fleetYardsCatalog = fleetYardsCatalog;
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public SlashCommandData getSlashCommandData()
    {
        return super.getSlashCommandData()
                .addSubcommands(
                        new SubcommandData(SUBCOMMAND_ADD, "Dodaj statek do swojego hangaru")
                                .addOption(OptionType.STRING, SHIP_PARAM, "Nazwa statku", true, true),
                        new SubcommandData(SUBCOMMAND_REMOVE, "Usuń statek z hangaru")
                                .addOption(OptionType.STRING, SHIP_PARAM, "Nazwa statku", true, true)
                                .addOption(OptionType.USER, USER_PARAM, "Gracz, któremu usunąć statek (tylko administrator)", false),
                        new SubcommandData(SUBCOMMAND_CLEAR, "Wyczyść cały hangar")
                                .addOption(OptionType.USER, USER_PARAM, "Gracz, którego hangar wyczyścić (tylko administrator)", false),
                        new SubcommandData(SUBCOMMAND_LIST, "Pokaż statki w hangarze")
                                .addOption(OptionType.USER, USER_PARAM, "Gracz, którego hangar pokazać (domyślnie ty)", false));
    }

    @Override
    public List<String> getAliases()
    {
        return List.of("hangar");
    }

    @Override
    public String getDescription()
    {
        return "Zarządzaj statkami w swoim hangarze";
    }

    @Override
    public boolean supports(CommandAutoCompleteInteractionEvent event)
    {
        // Default implementation does not see options nested in subcommands.
        return getAliases().contains(event.getName());
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) throws CommandException
    {
        if (!hasRequiredRole(event.getMember()))
            throw new CommandException("Brak wymaganej roli do użycia tej komendy.");

        if (SUBCOMMAND_ADD.equals(event.getSubcommandName()))
        {
            handleAddShip(event);
        }
        else if (SUBCOMMAND_REMOVE.equals(event.getSubcommandName()))
        {
            handleRemoveShip(event);
        }
        else if (SUBCOMMAND_CLEAR.equals(event.getSubcommandName()))
        {
            handleClearHangar(event);
        }
        else if (SUBCOMMAND_LIST.equals(event.getSubcommandName()))
        {
            handleListShips(event);
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) throws CommandException
    {
        if (!event.getFocusedOption().getName().equals(SHIP_PARAM))
            return;

        String query = event.getFocusedOption().getValue();
        if (SUBCOMMAND_ADD.equals(event.getSubcommandName()))
        {
            event.replyChoices(getCatalogChoices(query)).queue();
        }
        else if (SUBCOMMAND_REMOVE.equals(event.getSubcommandName()))
        {
            // W zdarzeniu autouzupełniania użytkownik z opcji nie jest
            // rozwiązany do encji — dostępne jest tylko surowe ID.
            OptionMapping userOption = event.getOption(USER_PARAM);
            long targetUserId = userOption != null ? userOption.getAsLong() : event.getUser().getIdLong();
            event.replyChoices(getOwnedShipChoices(targetUserId, query)).queue();
        }
    }

    private void handleAddShip(SlashCommandInteractionEvent event)
    {
        FleetYardsModel model = findShipModel(event.getOption(SHIP_PARAM).getAsString());

        UserShips userShips = Optional.ofNullable(fleetService.find(event.getUser().getIdLong()))
                .orElse(UserShips.builder().userId(event.getUser().getIdLong()).build());

        if (userShips.getShips().contains(model.slug()))
            throw new CommandException(format("Masz już statek '%s' w swoim hangarze.", model.name()));

        List<String> ships = new ArrayList<>(userShips.getShips());
        ships.add(model.slug());

        fleetService.save(userShips.toBuilder()
                .name(event.getMember().getEffectiveName())
                .ships(ships)
                .build());

        event.replyEmbeds(new EmbedBuilder()
                .setTitle("✅ Dodano statek")
                .setDescription(format("Statek **%s** został dodany do twojego hangaru.", model.name()))
                .setColor(Color.GREEN)
                .setTimestamp(dateTimeProvider.currentInstant())
                .build()).queue();
    }

    private void handleRemoveShip(SlashCommandInteractionEvent event)
    {
        User targetUser = resolveTargetUser(event);
        boolean self = targetUser.getIdLong() == event.getUser().getIdLong();

        String slug = event.getOption(SHIP_PARAM).getAsString();
        UserShips userShips = fleetService.find(targetUser.getIdLong());

        if (userShips == null || !userShips.getShips().contains(slug))
        {
            throw new CommandException(self
                    ? "Nie masz takiego statku w swoim hangarze."
                    : format("Gracz %s nie ma takiego statku w hangarze.", targetUser.getEffectiveName()));
        }

        List<String> ships = new ArrayList<>(userShips.getShips());
        ships.remove(slug);

        fleetService.save(userShips.toBuilder()
                .ships(ships)
                .build());

        event.replyEmbeds(new EmbedBuilder()
                .setTitle("🗑️ Usunięto statek")
                .setDescription(self
                        ? format("Statek **%s** został usunięty z twojego hangaru.", getShipName(slug))
                        : format("Statek **%s** został usunięty z hangaru gracza **%s**.", getShipName(slug), userShips.getName()))
                .setColor(Color.GREEN)
                .setTimestamp(dateTimeProvider.currentInstant())
                .build()).queue();
    }

    private void handleClearHangar(SlashCommandInteractionEvent event)
    {
        User targetUser = resolveTargetUser(event);
        boolean self = targetUser.getIdLong() == event.getUser().getIdLong();

        UserShips userShips = fleetService.find(targetUser.getIdLong());
        if (userShips == null || userShips.getShips().isEmpty())
            throw new CommandException("Hangar jest już pusty.");

        fleetService.save(userShips.toBuilder()
                .ships(new ArrayList<>())
                .build());

        event.replyEmbeds(new EmbedBuilder()
                .setTitle("🧹 Wyczyszczono hangar")
                .setDescription(self
                        ? "Twój hangar został wyczyszczony."
                        : format("Hangar gracza **%s** został wyczyszczony.", userShips.getName()))
                .setColor(Color.GREEN)
                .setTimestamp(dateTimeProvider.currentInstant())
                .build()).queue();
    }

    private void handleListShips(SlashCommandInteractionEvent event)
    {
        OptionMapping userOption = event.getOption(USER_PARAM);
        User targetUser = userOption != null ? userOption.getAsUser() : event.getUser();

        UserShips userShips = fleetService.find(targetUser.getIdLong());
        if (userShips == null || userShips.getShips().isEmpty())
        {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("🚀 Hangar - " + targetUser.getEffectiveName())
                    .setDescription("Brak statków w hangarze.")
                    .setColor(Color.RED)
                    .setTimestamp(dateTimeProvider.currentInstant())
                    .build()).queue();
            return;
        }

        String shipList = userShips.getShips().stream()
                .map(this::getShipName)
                .sorted()
                .map(shipName -> "• " + shipName)
                .collect(Collectors.joining("\n"));

        event.replyEmbeds(new EmbedBuilder()
                .setTitle("🚀 Hangar - " + targetUser.getEffectiveName())
                .setDescription("**Liczba statków:** `" + userShips.getShips().size() + "`\n\n" + shipList)
                .setColor(Color.GREEN)
                .setTimestamp(dateTimeProvider.currentInstant())
                .setThumbnail(targetUser.getEffectiveAvatarUrl())
                .build()).queue();
    }

    private User resolveTargetUser(SlashCommandInteractionEvent event)
    {
        OptionMapping userOption = event.getOption(USER_PARAM);
        if (userOption == null)
            return event.getUser();

        User targetUser = userOption.getAsUser();
        if (targetUser.getIdLong() != event.getUser().getIdLong() && !hasAdminRole(event.getMember()))
            throw new CommandException("Tylko administrator może zarządzać hangarem innego gracza.");

        return targetUser;
    }

    private boolean hasAdminRole(Member member)
    {
        return Optional.ofNullable(member)
                .map(Member::getRoles)
                .map(roles -> roles.stream()
                        .anyMatch(role -> role.getId().equals(this.adminRoleId)))
                .orElse(false);
    }

    private FleetYardsModel findShipModel(String shipNameOrSlug)
    {
        if (fleetYardsCatalog.isEmpty())
            throw new CommandException("Katalog statków nie został jeszcze załadowany. Spróbuj ponownie za chwilę.");

        return fleetYardsCatalog.findBySlug(shipNameOrSlug)
                .or(() -> fleetYardsCatalog.findByName(shipNameOrSlug))
                .orElseThrow(() -> new CommandException(format("Nie znaleziono statku '%s'.", shipNameOrSlug)));
    }

    private String getShipName(String slug)
    {
        return fleetYardsCatalog.findBySlug(slug)
                .map(FleetYardsModel::name)
                .orElse(slug);
    }

    private List<Command.Choice> getCatalogChoices(String query)
    {
        return fleetYardsCatalog.search(query, MAX_AUTOCOMPLETE_CHOICES).stream()
                .map(model -> new Command.Choice(model.name(), model.slug()))
                .toList();
    }

    private List<Command.Choice> getOwnedShipChoices(long userId, String query)
    {
        UserShips userShips = fleetService.find(userId);
        if (userShips == null)
            return List.of();

        final String lowerCaseQuery = query.toLowerCase();
        return userShips.getShips().stream()
                .map(slug -> new Command.Choice(getShipName(slug), slug))
                .filter(choice -> choice.getName().toLowerCase().contains(lowerCaseQuery))
                .sorted(Comparator.comparing(Command.Choice::getName))
                .limit(MAX_AUTOCOMPLETE_CHOICES)
                .toList();
    }
}
