package pl.tispmc.wolfie.discord.command.exception;

public class CommandException extends RuntimeException
{
    public CommandException()
    {
    }

    public CommandException(String message)
    {
        super(message);
    }
}
