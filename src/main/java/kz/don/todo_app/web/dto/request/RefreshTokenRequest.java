package kz.don.todo_app.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for refreshing access token")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token cannot be blank")
    @Schema(description = "Refresh token that was issued during authentication", required = true)
    private String refreshToken;
}