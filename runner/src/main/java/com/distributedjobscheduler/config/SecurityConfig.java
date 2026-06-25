package com.distributedjobscheduler.config;

import com.distributedjobscheduler.security.CustomUserDetailsService;
import com.distributedjobscheduler.security.JWTAuthenticationFilter;
import com.distributedjobscheduler.security.JWTUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize annotations
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
   /* @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Updated for Spring Security 6+
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // Public login/register
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Admin-only
                        .requestMatchers("/tasks/**").permitAll() // TEMPORARY for testing
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated() // All others need auth
                )
                .httpBasic(customizer -> {
                })
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class); // Add JWT filter

        return http.build();
    }
    */

    @Bean
    public JWTAuthenticationFilter jwtFilter() {
        return new JWTAuthenticationFilter(new JWTUtil(), new CustomUserDetailsService());
    }

}