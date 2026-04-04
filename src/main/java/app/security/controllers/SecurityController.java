package app.security.controllers;

import app.controllers.ControllerConstants;
import app.exceptions.ForbiddenException;
import app.exceptions.TokenVerificationException;
import app.exceptions.UnauthorizedException;
import app.security.dtos.LoginRequestDTO;
import app.security.dtos.LoginResponseDTO;
import app.security.dtos.RegisterRequestDTO;
import app.security.dtos.UserSecurityDTO;
import app.security.services.ISecurityService;
import app.security.utils.JWTUtil;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.Set;
import java.util.stream.Collectors;

public class SecurityController implements ISecurityController
{
    private static final String USER_ATTRIBUTE = ControllerConstants.USER_ATTRIBUTE;
    private final ISecurityService securityService;

    public SecurityController(ISecurityService securityService)
    {
        this.securityService = securityService;
    }

    @Override
    public void login(Context ctx)
    {
        LoginRequestDTO request = ctx.bodyAsClass(LoginRequestDTO.class);
        String token = securityService.login(request);
        ctx.status(HttpStatus.OK).json(new LoginResponseDTO(token));
    }

    @Override
    public void register(Context ctx)
    {
        RegisterRequestDTO request = ctx.bodyAsClass(RegisterRequestDTO.class);
        UserSecurityDTO user = securityService.register(request);
        ctx.status(HttpStatus.CREATED).json(user);
    }

    @Override
    public void authenticate(Context ctx)
    {
        // CORS preflight request
        if (ctx.method().toString().equals("OPTIONS"))
        {
            return;
        }

        // Endpoint open to ANYONE
        if (isOpenEndpoint(ctx))
        {
            return;
        }

        String token = extractToken(ctx);
        try
        {
            UserSecurityDTO user = JWTUtil.parseToken(token);
            ctx.attribute(USER_ATTRIBUTE, user);
        }
        catch (TokenVerificationException e)
        {
            throw new UnauthorizedException("Invalid or expired token", e);
        }
    }

    @Override
    public void authorize(Context ctx)
    {
        // Endpoint open to ANYONE
        if (isOpenEndpoint(ctx))
        {
            return;
        }

        UserSecurityDTO user = ctx.attribute(USER_ATTRIBUTE);
        validUser(user);
        validRole(ctx, user);
    }

    private boolean isOpenEndpoint(Context ctx)
    {
        Set<String> allowedRoles = ctx.routeRoles().stream()
                .map(role -> role.toString().toUpperCase())
                .collect(Collectors.toSet());

        return allowedRoles.contains("ANYONE");
    }

    private String extractToken(Context ctx)
    {
        String header = ctx.header("Authorization");
        if (header == null)
        {
            throw new UnauthorizedException("Authorization header is missing");
        }

        String[] parts = header.split(" ");
        if (parts.length < 2 || parts[1].isBlank())
        {
            throw new UnauthorizedException("Authorization header is malformed");
        }

        return parts[1];
    }

    private void validUser(UserSecurityDTO user)
    {
        if (user == null)
        {
            throw new UnauthorizedException("No user found on request context");
        }
    }

    private void validRole(Context ctx, UserSecurityDTO user)
    {
        boolean hasRole = user.roles().stream()
                .anyMatch(role -> ctx.routeRoles().contains(role));

        if (!hasRole)
        {
            throw new ForbiddenException("Access denied: required roles are " + ctx.routeRoles());
        }
    }
}
