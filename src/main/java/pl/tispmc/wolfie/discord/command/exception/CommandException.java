package pl.tispmc.wolfie.discord.command.exception;

public class CommandException extends RuntimeException
{
    private final boolean ephemeral;

    public CommandException()
    {
        this.ephemeral = false;
    }

    public CommandException(String message)
    {
        super(message);
        this.ephemeral = false;
    }

    public CommandException(String message, boolean ephemeral)
    {
        super(message);
        this.ephemeral = ephemeral;
    }

    public boolean isEphemeral()
    {
        return ephemeral;
    }
}
