package pl.tispmc.wolfie.web.model;

import lombok.Data;

@Data
public class RestErrorResponse
{
    private String message;

    public static RestErrorResponse of(String message)
    {
        RestErrorResponse response = new RestErrorResponse();
        response.setMessage(message);
        return response;
    }
}
