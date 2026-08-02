package com.legacyvault.legacyvault.controller;

import com.legacyvault.legacyvault.model.User;
import com.legacyvault.legacyvault.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // REGISTER
    @PostMapping("/register")
    public User register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password) {

        return userService.registerUser(
                name,
                email,
                password
        );
    }

    // LOGIN
    @PostMapping("/login")
    public User login(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request) {

        // Check email + password
        User user = userService.loginUser(
                email,
                password
        );

        // Convert database role into Spring Security authority
        String authority =
                "ROLE_" + user.getRole().name();

        SimpleGrantedAuthority grantedAuthority =
                new SimpleGrantedAuthority(authority);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        List.of(grantedAuthority)
                );

        SecurityContext securityContext =
                SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(securityContext);

        // Store authentication in HTTP session
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository
                        .SPRING_SECURITY_CONTEXT_KEY,
                securityContext
        );

        return user;
    }
}