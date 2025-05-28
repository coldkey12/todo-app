package kz.don.todo_app.dto;

import kz.don.todo_app.enums.RoleEnum;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String username;
    private RoleEnum role;
}
