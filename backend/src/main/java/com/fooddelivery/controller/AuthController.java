
package com.fooddelivery.controller;

import com.fooddelivery.service.AuthService;
import com.fooddelivery.service.AuthService.AuthResponse;
import com.fooddelivery.service.AuthService.CustomerProfile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Register a new customer
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerProfile register(
            @Valid @RequestBody RegisterRequest request) {

        return authService.register(
                request.name(),
                request.email(),
                request.password()
        );
    }

    // Log in and return a JWT token
    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(
                request.email(),
                request.password()
        );
    }

    public record RegisterRequest(

            @NotBlank(message = "Name is required")
            @Size(max = 100)
            String name,

            @NotBlank(message = "Email is required")
            @Email(message = "Enter a valid email address")
            @Size(max = 150)
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 72)
            String password
    ) {
    }

    public record LoginRequest(

            @NotBlank(message = "Email is required")
            @Email(message = "Enter a valid email address")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {
    }
}
