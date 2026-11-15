package com.filevault.service;

import com.filevault.dto.AuthResponse;
import com.filevault.dto.LoginRequest;
import com.filevault.dto.RegisterRequest;
import com.filevault.entity.RefreshToken;
import com.filevault.entity.User;
import com.filevault.repository.RefreshTokenRepository;
import com.filevault.repository.UserRepository;
import com.filevault.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public record AuthResult(AuthResponse response, String refreshToken) {}

    @Transactional
    public AuthResult register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        User user = User.builder()
            .fullName(request.getFullName().trim())
            .email(normalizedEmail)
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role("ROLE_USER")
            .build();

        User savedUser = userRepository.save(user);
        return issueTokensForUser(savedUser);
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        refreshTokenRepository.deleteByUser(user);
        return issueTokensForUser(user);
    }

    @Transactional
    public AuthResult refreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BadCredentialsException("Refresh token is missing.");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(rawRefreshToken)
            .orElseThrow(() -> new BadCredentialsException("Invalid refresh token."));

        if (storedToken.isRevoked() || storedToken.getExpiryDate().isBefore(Instant.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new BadCredentialsException("Refresh token has expired or been revoked.");
        }

        // Rotate refresh token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        return issueTokensForUser(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByToken(rawRefreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private AuthResult issueTokensForUser(User user) {
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getId(), user.getRole());
        String refreshTokenValue = jwtService.generateRefreshToken(user.getEmail(), user.getId());

        RefreshToken refreshTokenEntity = RefreshToken.builder()
            .token(refreshTokenValue)
            .user(user)
            .expiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()))
            .revoked(false)
            .build();

        refreshTokenRepository.save(refreshTokenEntity);

        AuthResponse authResponse = AuthResponse.builder()
            .accessToken(accessToken)
            .user(AuthResponse.UserSummary.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .name(user.getFullName())
                .email(user.getEmail())
                .avatarInitials(computeInitials(user.getFullName()))
                .build())
            .build();

        return new AuthResult(authResponse, refreshTokenValue);
    }

    private String computeInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "U";
        }
        String initials = Arrays.stream(fullName.trim().split("\\s+"))
            .filter(part -> !part.isEmpty())
            .map(part -> String.valueOf(Character.toUpperCase(part.charAt(0))))
            .limit(2)
            .collect(Collectors.joining());
        return initials.isEmpty() ? "U" : initials;
    }
}
