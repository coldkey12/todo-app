package kz.don.todo_app.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import kz.don.todo_app.domain.enums.RoleEnum;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authentication response object")
public class AuthResponse {
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "Unique identifier of the authenticated user", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;

    @Schema(description = "Username of the authenticated user", example = "john_doe")
    private String username;

    @Schema(
            description = "Role of the authenticated user",
            example = "USER",
            allowableValues = {"USER", "ADMIN"}
    )
    private RoleEnum role;
}