package kz.don.todo_app.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import kz.don.todo_app.domain.enums.StatusEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Task response object")
public class TaskResponse {
    @Schema(description = "Unique identifier of the task", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Title of the task", example = "Complete project documentation")
    private String title;

    @Schema(description = "Detailed description of the task", example = "Write all API documentation with examples")
    private String description;

    @Schema(
            description = "Current status of the task",
            example = "IN_PROGRESS",
            allowableValues = {"TODO", "IN_PROGRESS", "DONE"}
    )
    private StatusEnum status;

    @Schema(description = "Date and time when the task was created", example = "2024-03-15T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time of last task update", example = "2024-03-15T16:45:00")
    private LocalDateTime updatedAt;
}