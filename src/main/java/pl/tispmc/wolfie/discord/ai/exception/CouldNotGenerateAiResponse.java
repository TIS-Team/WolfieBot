package pl.tispmc.wolfie.discord.ai.exception;

public class CouldNotGenerateAiResponse extends Exception
{
    public CouldNotGenerateAiResponse(String message)
    {
        super(message);
    }

    public CouldNotGenerateAiResponse(String message, Throwable cause)
    {
        super(message, cause);
    }
}
