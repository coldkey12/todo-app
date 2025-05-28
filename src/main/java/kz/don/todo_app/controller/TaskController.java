package kz.don.todo_app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.don.todo_app.enums.StatusEnum;
import kz.don.todo_app.dto.TaskRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import kz.don.todo_app.dto.TaskResponse;
import kz.don.todo_app.service.TaskService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Endpoints for task management")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Get user's tasks", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @Parameter(description = "Filter tasks by status", example = "IN_PROGRESS")
            @RequestParam(required = false) StatusEnum status
    ) {
        return ResponseEntity.ok(taskService.getUserTasks(status));
    }

    @Operation(summary = "Create new task", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Task created successfully")
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Task data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TaskRequest.class))
            )
            @Valid @RequestBody TaskRequest request
    ) {
        return ResponseEntity.ok(taskService.createTask(request));
    }

    @Operation(summary = "Update task", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Task updated successfully")
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "Task ID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated task data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TaskRequest.class))
            )
            @Valid @RequestBody TaskRequest request
    ) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @Operation(summary = "Delete task", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Task deleted successfully")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task ID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id
    ) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}