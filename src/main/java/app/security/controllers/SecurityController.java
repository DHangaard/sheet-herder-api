package app.security.controllers;

import app.exceptions.ForbiddenException;
import app.exceptions.TokenVerificationException;
import app.exceptions.UnauthorizedException;
import app.security.dtos.LoginRequestDTO;
import app.security.dtos.LoginResponseDTO;
import app.security.dtos.RegisterRequestDTO;
import app.security.dtos.UserDTO;
import app.security.services.ISecurityService;
import app.security.utils.JWTUtil;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.Set;
import java.util.stream.Collectors;

public class SecurityController implements ISecurityController
{
    private static final String USER_ATTRIBUTE = "user";
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
        UserDTO user = securityService.register(request);
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

        // Endpoint unprotected or open to ANYONE
        if (isOpenEndpoint(ctx))
        {
            return;
        }

        String token = extractToken(ctx);
        try
        {
            UserDTO user = JWTUtil.parseToken(token);
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
        // Endpoint unprotected or open to ANYONE
        if (isOpenEndpoint(ctx))
        {
            return;
        }

        UserDTO user = ctx.attribute(USER_ATTRIBUTE);
        validUser(user);
        validRole(ctx, user);
    }

    private boolean isOpenEndpoint(Context ctx)
    {
        Set<String> allowedRoles = ctx.routeRoles().stream()
                .map(role -> role.toString().toUpperCase())
                .collect(Collectors.toSet());

        return allowedRoles.isEmpty() || allowedRoles.contains("ANYONE");
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

    private void validUser(UserDTO user)
    {
        if (user == null)
        {
            throw new ForbiddenException("No user found on request context");
        }
    }

    private void validRole(Context ctx, UserDTO user)
    {
        boolean hasRole = user.roles().stream()
                .anyMatch(role -> ctx.routeRoles().contains(role));

        if (!hasRole)
        {
            throw new ForbiddenException("Access denied: required roles are " + ctx.routeRoles());
        }
    }
}
