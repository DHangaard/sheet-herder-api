package app.exceptions;

import io.javalin.http.HttpStatus;

public class ExternalApiException extends ApiException
{
    public ExternalApiException(String message)
    {
        super(HttpStatus.BAD_GATEWAY.getCode(), message);
    }
}
