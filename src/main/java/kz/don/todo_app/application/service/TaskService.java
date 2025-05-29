package kz.don.todo_app.application.service;

import jakarta.persistence.EntityNotFoundException;
import kz.don.todo_app.domain.entity.Task;
import kz.don.todo_app.domain.entity.User;
import kz.don.todo_app.domain.enums.RoleEnum;
import kz.don.todo_app.domain.enums.StatusEnum;
import kz.don.todo_app.domain.repository.TaskRepository;
import kz.don.todo_app.domain.repository.UserRepository;
import kz.don.todo_app.web.dto.request.TaskRequest;
import kz.don.todo_app.web.dto.response.TaskResponse;
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

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private void checkTaskOwnership(Task task) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == RoleEnum.ADMIN) {
            return;
        }

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