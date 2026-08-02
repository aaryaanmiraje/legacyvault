package com.legacyvault.legacyvault.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // PUBLIC ENDPOINTS
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/beneficiary-verification/verify",

                    // Beneficiary token redemption
                    "/api/beneficiary-access/redeem",

                    // Spring Security allows request through,
                    // controller checks BENEFICIARY_ID session
                    "/api/beneficiary-access/vault",

                    "/error"
                ).permitAll()

                // ADMIN ONLY
                .requestMatchers(
                    "/api/admin/**"
                ).hasRole("ADMIN")

                // EVERYTHING ELSE REQUIRES NORMAL LOGIN
                .anyRequest().authenticated()
            );

        return http.build();
    }
}