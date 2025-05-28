package kz.don.todo_app.service;

import kz.don.todo_app.dto.AuthResponse;
import kz.don.todo_app.dto.RefreshTokenRequest;
import kz.don.todo_app.enums.RoleEnum;
import kz.don.todo_app.model.RefreshToken;
import kz.don.todo_app.model.User;
import kz.don.todo_app.repository.RefreshTokenRepository;
import kz.don.todo_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import kz.don.todo_app.dto.RegisterRequest;
import kz.don.todo_app.dto.AuthRequest;
import org.springframework.security.core.Authentication;

import java.rmi.AccessException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;



    public AuthResponse register(RegisterRequest request) throws AccessException {
        if (userRepository.findByUsername(request.getUsername())!=null) {
            throw new AccessException("Username already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleEnum.USER)
                .build();

        user = userRepository.save(user);
        log.info("User registered: {}", user.getUsername());

        return generateAuthResponse(user);
    }

    public AuthResponse login(AuthRequest request) {
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
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) throws Exception {
        if (!jwtService.validateToken(request.getRefreshToken())) {
            throw new Exception("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken());
        if (refreshToken == null) {
            throw new Exception("Refresh token not found");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new Exception("Refresh token expired");
        }

        User user = refreshToken.getUser();

        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshExpiration()))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken);
        if (refreshToken != null) {
            refreshTokenRepository.deleteByToken(refreshToken);
            log.info("User logged out, refresh token deleted: {}", refreshToken);
        } else {
            log.warn("Attempted to logout with invalid or non-existent refresh token: {}", refreshToken);
        }
    }
}