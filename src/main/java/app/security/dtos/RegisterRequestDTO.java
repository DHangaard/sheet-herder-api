package app.security.dtos;

public record RegisterRequestDTO(
        String email,
        String username,
        String password
)
{
}
