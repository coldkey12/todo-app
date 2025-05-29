package kz.don.todo_app.domain.repository;

import kz.don.todo_app.domain.enums.StatusEnum;
import kz.don.todo_app.domain.entity.Task;
import kz.don.todo_app.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByUserOrderByCreatedAtDesc(User currentUser);

    List<Task> findByUserAndStatusOrderByCreatedAtDesc(User currentUser, StatusEnum status);

    List<Task> findByUserId(UUID userId);
}
