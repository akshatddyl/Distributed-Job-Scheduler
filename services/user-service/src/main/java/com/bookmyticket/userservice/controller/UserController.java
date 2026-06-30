package com.bookmyticket.userservice.controller;

import com.bookmyticket.shared.dto.ResponseWrapper;
import com.bookmyticket.userservice.dto.AuthResponse;
import com.bookmyticket.userservice.dto.LoginRequest;
import com.bookmyticket.userservice.dto.RegisterRequest;
import com.bookmyticket.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ResponseWrapper<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseWrapper.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = userService.login(request);

        return ResponseEntity.ok(ResponseWrapper.success("Login successful", response));
    }
}
