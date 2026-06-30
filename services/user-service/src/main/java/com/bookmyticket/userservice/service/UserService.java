package com.bookmyticket.userservice.service;

import com.bookmyticket.userservice.dto.AuthResponse;
import com.bookmyticket.userservice.dto.LoginRequest;
import com.bookmyticket.userservice.dto.RegisterRequest;
import com.bookmyticket.userservice.entity.Role;
import com.bookmyticket.userservice.entity.User;
import com.bookmyticket.userservice.repository.UserRepository;
import com.bookmyticket.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for user registration and authentication.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registers a new user. Defaults role to {@link Role#CUSTOMER}.
     *
     * @throws IllegalArgumentException if the email is already registered
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered: " + request.email());
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new AuthResponse(user.getId(), user.getEmail(), user.getRole().name(), token);
    }

    /**
     * Authenticates a user by email and password and returns a JWT.
     *
     * @throws IllegalArgumentException if credentials are invalid
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(user.getId(), user.getEmail(), user.getRole().name(), token);
    }
}
