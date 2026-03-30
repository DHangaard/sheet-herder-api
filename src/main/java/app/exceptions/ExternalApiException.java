package app.exceptions;

public class ExternalApiException extends ApiException
{
    public ExternalApiException(int code, String message)
    {
        super(code, message);
    }
}
