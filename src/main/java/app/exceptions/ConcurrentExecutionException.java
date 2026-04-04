package app.exceptions;

import io.javalin.http.HttpStatus;

public class ConcurrentExecutionException extends ApiException
{
    public ConcurrentExecutionException(String message, Throwable cause)
    {
        super(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), message, cause);
    }
}
