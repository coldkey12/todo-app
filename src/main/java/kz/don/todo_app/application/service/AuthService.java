package kz.don.todo_app.application.service;

import kz.don.todo_app.domain.entity.RefreshToken;
import kz.don.todo_app.domain.entity.User;
import kz.don.todo_app.domain.enums.RoleEnum;
import kz.don.todo_app.domain.repository.RefreshTokenRepository;
import kz.don.todo_app.domain.repository.UserRepository;
import kz.don.todo_app.infrastructure.security.jwt.JwtService;
import kz.don.todo_app.web.dto.request.AuthRequest;
import kz.don.todo_app.web.dto.request.RefreshTokenRequest;
import kz.don.todo_app.web.dto.request.RegisterRequest;
import kz.don.todo_app.web.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthResponse register(RegisterRequest request) throws Exception {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new Exception("Username already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .role(RoleEnum.USER)
                .build();

        user = userRepository.save(user);
        log.info("User registered: {}", user.getUsername());

        return generateAuthResponse(user);
    }

    public AuthResponse login(AuthRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();
            log.info("User logged in: {}", user.getUsername());
            return generateAuthResponse(user);
        } catch (BadCredentialsException e) {
            log.error("Bad credentials for user: {}", request.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password", e);
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getUsername(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Authentication failed", e);
        }
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        try {
            if (!jwtService.isTokenStructureValid(request.getRefreshToken())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token format");
            }

            UUID userId = jwtService.getUserIdFromTokenIgnoreExpiration(request.getRefreshToken());

            Optional<RefreshToken> optionalToken = refreshTokenRepository.findByToken(request.getRefreshToken());
            if (optionalToken.isEmpty()) {
                optionalToken = refreshTokenRepository.findByUserId(userId);
            }

            RefreshToken refreshToken = optionalToken.orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found"));

            if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
                log.warn("Refresh token expired for user: {}", userId);
                refreshTokenRepository.delete(refreshToken);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
            }

            if (!jwtService.validateTokenSignature(request.getRefreshToken())) {
                log.warn("Invalid refresh token signature for user: {}", userId);
                refreshTokenRepository.delete(refreshToken);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token signature");
            }

            User user = refreshToken.getUser();
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user associated with token");
            }

            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            refreshToken.setToken(newRefreshToken);
            refreshToken.setExpiryDate(Instant.now().plusMillis(jwtService.getRefreshExpiration()));
            refreshTokenRepository.save(refreshToken);

            log.info("Refreshed tokens for user: {}", user.getUsername());

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .build();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error refreshing token", e);
        }
    }

    private AuthResponse generateAuthResponse(User user) {

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> RefreshToken.builder().user(user).build());

        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setExpiryDate(Instant.now().plusMillis(jwtService.getRefreshExpiration()));

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    public void cleanExpiredRefreshTokens() {
        Instant now = Instant.now();
        List<RefreshToken> expiredTokens = refreshTokenRepository
                .findByExpiryDateBefore(now);

        if (!expiredTokens.isEmpty()) {
            log.info("Cleaning up {} expired refresh tokens", expiredTokens.size());
            refreshTokenRepository.deleteAll(expiredTokens);
        }
    }

    public void logout(RefreshTokenRequest request) {
        try {
            if (!jwtService.isTokenStructureValid(request.getRefreshToken())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token format");
            }

            Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken());
            if (refreshToken.isPresent()) {
                refreshTokenRepository.delete(refreshToken.get());
                log.info("User logged out successfully");
            } else {
                log.warn("Refresh token not found during logout");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
            }

            SecurityContextHolder.clearContext();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during logout", e);
        }
    }
}