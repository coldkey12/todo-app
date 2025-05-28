package kz.don.todo_app.repository;

import kz.don.todo_app.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    RefreshToken findByToken(String token);

    void deleteByToken(String token);
}
