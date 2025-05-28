package kz.don.todo_app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User response object")
public class UserResponse {

    @Schema(description = "Unique identifier of the user", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Username of the user", example = "john_doe")
    private String username;

    @Schema(
            description = "Role of the user",
            example = "ADMIN",
            allowableValues = {"USER", "ADMIN"}
    )
    private String role;

    @Schema(description = "Account activation status", example = "true")
    private Boolean enabled;

    @Schema(description = "Date and time when the account was created", example = "2023-07-15T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time of last account update", example = "2023-07-20T09:15:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Number of tasks assigned to the user", example = "5")
    private Integer taskCount;
}