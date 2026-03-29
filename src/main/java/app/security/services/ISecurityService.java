package app.security.services;

import app.security.dtos.LoginRequestDTO;
import app.security.dtos.RegisterRequestDTO;
import app.security.dtos.UserDTO;

public interface ISecurityService
{
    UserDTO register(RegisterRequestDTO request);
    String login(LoginRequestDTO request);
}
