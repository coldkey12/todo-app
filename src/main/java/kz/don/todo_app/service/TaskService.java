package kz.don.todo_app.service;

import jakarta.persistence.EntityNotFoundException;
import kz.don.todo_app.dto.TaskResponse;
import kz.don.todo_app.enums.StatusEnum;
import kz.don.todo_app.model.Task;
import kz.don.todo_app.dto.TaskRequest;
import kz.don.todo_app.model.User;
import kz.don.todo_app.repository.TaskRepository;
import kz.don.todo_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<TaskResponse> getUserTasks(StatusEnum status) {
        User currentUser = getCurrentUser();

        List<Task> tasks = (status == null)
                ? taskRepository.findByUserOrderByCreatedAtDesc(currentUser)
                : taskRepository.findByUserAndStatusOrderByCreatedAtDesc(currentUser, status);

        log.info("Retrieved {} tasks for user: {}", tasks.size(), currentUser.getUsername());
        return tasks.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponse createTask(TaskRequest request) {
        User currentUser = getCurrentUser();

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : StatusEnum.TODO)
                .user(currentUser)
                .build();

        task = taskRepository.save(task);
        log.info("Task created: {}", task.getId());
        return mapToResponse(task);
    }

    public TaskResponse updateTask(UUID id, TaskRequest request) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        checkTaskOwnership(task);

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        task = taskRepository.save(task);
        log.info("Task updated: {}", task.getId());
        return mapToResponse(task);
    }

    public void deleteTask(UUID id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        checkTaskOwnership(task);

        taskRepository.delete(task);
        log.info("Task deleted: {}", id);
    }

    public TaskResponse getTaskById(UUID id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        checkTaskOwnership(task);

        return mapToResponse(task);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.isEmpty()) {
            throw new AccessDeniedException("User not authenticated");
        }
        return userRepository.findByUsername(username);
    }

    private void checkTaskOwnership(Task task) {
        User currentUser = getCurrentUser();
        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to modify this task");
        }
    }

    private TaskResponse mapToResponse(Task task) {
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