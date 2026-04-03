package app.security.dtos;

import app.security.enums.Role;

import java.util.Set;

public record UserSecurityDTO(
        Long id,
        String username,
        Set<Role> roles
)
{
}
