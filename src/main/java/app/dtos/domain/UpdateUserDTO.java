package app.dtos.domain;

public record UpdateUserDTO(
        String email,
        String username,
        String password
)
{
}
