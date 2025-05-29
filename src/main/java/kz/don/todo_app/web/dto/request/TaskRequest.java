package kz.don.todo_app.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kz.don.todo_app.domain.enums.StatusEnum;
import lombok.Data;

@Data
@Schema(description = "Request object for creating or updating a task")
public class TaskRequest {

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    @Schema(description = "Title of the task", example = "Complete project documentation", required = true)
    @JsonProperty(required = true)
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Detailed description of the task", example = "Write all API documentation with examples")
    private String description;

    @Schema(
            description = "Current status of the task",
            example = "IN_PROGRESS",
            allowableValues = {"TODO", "IN_PROGRESS", "DONE"}
    )
    private StatusEnum status;
}