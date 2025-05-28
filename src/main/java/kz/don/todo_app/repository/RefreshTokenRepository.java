package kz.don.todo_app.repository;

import kz.don.todo_app.model.RefreshToken;
import kz.don.todo_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    Optional<RefreshToken> findByUser(User user);
    void deleteByUser(User user);

    Optional<RefreshToken> findByUserId(UUID userId);

    List<RefreshToken> findByExpiryDateBefore(Instant now);
}
