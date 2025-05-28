package kz.don.todo_app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.don.todo_app.dto.AuthResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import kz.don.todo_app.dto.AuthRequest;
import kz.don.todo_app.dto.RegisterRequest;
import kz.don.todo_app.dto.RefreshTokenRequest;
import kz.don.todo_app.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register new user")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterRequest.class))
            )
            @Valid @RequestBody RegisterRequest request
    ) throws Exception {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Authenticate user")
    @ApiResponse(responseCode = "200", description = "User authenticated successfully")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthRequest.class))
            )
            @Valid @RequestBody AuthRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Refresh access token")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class))
            )
            @Valid @RequestBody RefreshTokenRequest request
    ) throws Exception {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(summary = "Logout user")
    @ApiResponse(responseCode = "200", description = "User logged out successfully")
    @ApiResponse(responseCode = "401", description = "Invalid or missing refresh token")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class))
            )
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }
}