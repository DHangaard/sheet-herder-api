package app.exceptions;

import io.javalin.http.HttpStatus;

public class UnauthorizedException extends ApiException
{
    public UnauthorizedException(String message)
    {
        super(HttpStatus.UNAUTHORIZED.getCode(), message);
    }
}
