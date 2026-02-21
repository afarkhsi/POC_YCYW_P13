package com.poc.chat.controller;

import com.poc.chat.config.JwtUtils;
import com.poc.chat.dto.AuthDTO;
import com.poc.chat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        // @Valid pour déclencher la validation Bean Validation
        return userService.findByEmail(request.getEmail())
                .filter(user -> userService.checkPassword(request.getPassword(), user.getPassword()))
                .map(user -> {
                    String token = jwtUtils.generateToken(
                            user.getId(), user.getEmail(), user.getType()
                    );
                    return ResponseEntity.ok(new AuthDTO.LoginResponse(
                            token,
                            user.getId(),
                            user.getFirstname(),
                            user.getLastname(),
                            user.getType()
                    ));
                })
                .orElse(ResponseEntity.status(401).build());
    }
}
