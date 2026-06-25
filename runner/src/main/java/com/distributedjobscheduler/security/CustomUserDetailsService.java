package com.distributedjobscheduler.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username.equals("admin")) {
            return User.builder()
                    .username("admin")
                    .password("{noop}password")
                    .roles("ADMIN")
                    .build();
        } else {
            return User.builder()
                    .username("user")
                    .password("{noop}password")
                    .roles("USER")
                    .build();
        }
    }
}

/**
 * Use of this component
 *
 *
 * The `CustomUserDetailsService` component is a **custom authentication
 * provider** used by Spring Security. It implements `UserDetailsService` to
 * **load user details during login**.
 *
 * ---
 *
 * ### ✅ **What It Does:**
 *
 * * Provides user credentials (username, password, roles) to Spring Security
 * when a user logs in.
 * * In this example, it **hardcodes two users**:
 *
 * * `admin` → password: `password`, role: `ADMIN`
 * * any other username → password: `password`, role: `USER`
 *
 * ---
 *
 * ### 🧠 **Why It's Needed:**
 *
 * Spring Security calls `loadUserByUsername()` to:
 *
 * * Authenticate users
 * * Assign roles/authorities
 * * Protect routes based on roles
 *
 * ---
 *
 * ### 🔐 Password Note:
 *
 * The prefix `{noop}` tells Spring Security that the password is in **plain
 * text** (not encoded). In real apps, use encrypted passwords.
 *
 * ---
 *
 * ### 🧪 Example Usage:
 *
 * Login with:
 *
 * * Username: `admin`, Password: `password` → gets `ROLE_ADMIN`
 * * Username: `john`, Password: `password` → gets `ROLE_USER`
 *
 * Used typically with `HttpSecurity` config in `SecurityConfig` to secure
 * endpoints.
 *
 **/

/**
 *
 * ### 🔐 **Component**: `CustomUserDetailsService.java`
 **
 * Purpose**:
 * Provides **in-memory user authentication** for Spring Security.
 **
 * How It Works**:
 *
 * Implements `UserDetailsService` and overrides `loadUserByUsername`.
 * Returns:
 *
 * `admin` with role `ADMIN`
 * any other username as `USER`
 * Passwords use `{noop}` → plain text (for development only).
 **
 * Example Credentials**:
 *
 * | Username | Password | Role |
 * | -------- | -------- | ----- |
 * | admin | password | ADMIN |
 * | test | password | USER |
 **
 * Used in**: `SecurityConfig.java` via Spring Security filter chain.
 *
 *
 *
 **/