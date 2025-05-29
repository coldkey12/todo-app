package kz.don.todo_app.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for user authentication")
public class AuthRequest {

    @NotBlank(message = "Username cannot be blank")
    @Schema(description = "Username of the user", example = "john_doe", required = true)
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Schema(description = "Password of the user", example = "SecurePass123!", required = true)
    private String password;
}