package com.poc.chat.controller;

import com.poc.chat.dto.AuthDTO;
import com.poc.chat.model.User;
import com.poc.chat.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDTO.LoginRequest request) {
        return userService.findByEmail(request.getEmail())
                .filter(user -> userService.checkPassword(request.getPassword(), user.getPassword()))
                .map(user -> {
                    String token = Jwts.builder()
                            .setSubject(user.getEmail())
                            .claim("userId", user.getId())
                            .claim("type", user.getType())
                            .setIssuedAt(new Date())
                            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                            .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)),
                                    SignatureAlgorithm.HS256)
                            .compact();
                    return ResponseEntity.ok(new AuthDTO.LoginResponse(
                            token, user.getId(),
                            user.getFirstname(), user.getLastname(),
                            user.getType()
                    ));
                })
                .orElse(ResponseEntity.status(401).build());
    }
}
