package com.portfolio.controller;

import com.portfolio.dto.request.CreateUserRequest;
import com.portfolio.dto.request.LoginRequest;
import com.portfolio.dto.response.AuthResponse;
import com.portfolio.exception.ErrorResponse;
import com.portfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirements
    @Operation(
            summary = "Register a new user",
            description = "Creates a new account and returns a JWT token. No authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g. invalid email, short password)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Email already registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody CreateUserRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirements
    @Operation(
            summary = "Authenticate and get JWT token",
            description = "Returns a 24-hour JWT token. Use it as `Authorization: Bearer <token>` on all other endpoints."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "token": "eyJhbGci...",
                                      "userId": 1,
                                      "email": "user@example.com",
                                      "firstName": "John",
                                      "lastName": "Doe"
                                    }"""))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Authentication attempt for user: {}", request.getEmail());
        AuthResponse response = userService.authenticate(request);
        return ResponseEntity.ok(response);
    }
}
