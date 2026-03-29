package app.security.dtos;

import app.security.enums.Role;

import java.util.Set;

public record UserDTO(
        String username,
        Set<Role> roles
)
{
}
