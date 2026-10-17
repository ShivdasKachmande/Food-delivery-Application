
package com.fooddelivery.service;

import com.fooddelivery.entity.Customer;
import com.fooddelivery.repository.CustomerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public AuthService(
            CustomerRepository customerRepository,
            JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.jwtService = jwtService;
    }

    // Register a new customer
    public CustomerProfile register(
            String name,
            String email,
            String password) {

        if (name == null || name.isBlank() || name.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Name is required and must not exceed 100 characters");
        }

        if (email == null
                || email.length() > 150
                || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Enter a valid email address");
        }

        if (password == null
                || password.length() < 8
                || password.length() > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain 8 to 72 characters");
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        if (customerRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists");
        }

        String passwordHash = passwordEncoder.encode(password);

        // Public registration always creates a CUSTOMER account.
        Customer customer = new Customer(
                name.trim(),
                normalizedEmail,
                passwordHash
        );

        Customer savedCustomer = customerRepository.save(customer);

        return toProfile(savedCustomer);
    }

    // Authenticate a customer and issue a role-aware JWT
    public AuthResponse login(String email, String password) {

        if (email == null || email.isBlank()
                || password == null || password.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email and password are required");
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        Customer customer = customerRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid email or password"));

        if (!passwordEncoder.matches(
                password,
                customer.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        // Read the role from the customer record in the database.
        String role = customer.getRole()
                .trim()
                .toUpperCase(Locale.ROOT);

        // Do not issue tokens for unexpected roles.
        if (!role.equals("CUSTOMER") && !role.equals("ADMIN")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Invalid account role");
        }

        // IMPORTANT: Pass TWO arguments so the role is put in the JWT.
        String token = jwtService.generateToken(
                customer.getEmail(),
                role
        );

        CustomerProfile profile = toProfile(customer);

        // Include the role in the response to help verify the result.
        return new AuthResponse(
                profile.id(),
                profile.name(),
                profile.email(),
                role,
                token,
                "Bearer"
        );
    }

    private CustomerProfile toProfile(Customer customer) {
        return new CustomerProfile(
                customer.getId(),
                customer.getName(),
                customer.getEmail()
        );
    }

    public record CustomerProfile(
            Long id,
            String name,
            String email) {
    }

    public record AuthResponse(
            Long id,
            String name,
            String email,
            String role,
            String token,
            String tokenType) {
    }
}
