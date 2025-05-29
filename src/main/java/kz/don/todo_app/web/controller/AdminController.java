package kz.don.todo_app.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import kz.don.todo_app.web.dto.response.TaskResponse;
import kz.don.todo_app.web.dto.response.UserResponse;
import kz.don.todo_app.domain.entity.Task;
import kz.don.todo_app.domain.entity.User;
import kz.don.todo_app.domain.repository.TaskRepository;
import kz.don.todo_app.domain.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Controller", description = "Endpoints for admin operations")
public class AdminController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public AdminController(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Operation(summary = "Get all users", description = "Returns a list of all registered users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    })
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users.stream()
                .map(this::mapToUserResponse)
                .toList());
    }

    @Operation(summary = "Update user status", description = "Enable or disable a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User status updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @Parameter(description = "UUID of the user to update") @PathVariable UUID userId,
            @Parameter(description = "Set to true to enable, false to disable") @RequestParam boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setEnabled(enabled);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all tasks", description = "Returns all tasks, optionally filtered by userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of tasks retrieved successfully")
    })
    @GetMapping("/tasks")
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @Parameter(description = "Optional UUID of user to filter tasks by") @RequestParam(required = false) UUID userId) {

        List<Task> tasks = userId != null
                ? taskRepository.findByUserId(userId)
                : taskRepository.findAll();

        return ResponseEntity.ok(tasks.stream()
                .map(this::mapToTaskResponse)
                .toList());
    }

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .taskCount(user.getTasks() != null ? user.getTasks().size() : 0)
                .build();
    }

    private TaskResponse mapToTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

}