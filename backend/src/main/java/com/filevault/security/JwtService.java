package com.filevault.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final long VAULT_TOKEN_EXPIRATION_MS = 600_000L; // 10 minutes

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-expiration-ms:900000}") long accessTokenExpirationMs,
        @Value("${jwt.refresh-expiration-ms:604800000}") long refreshTokenExpirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(String email, UUID userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
            .subject(email)
            .claim("uid", userId.toString())
            .claim("role", role)
            .claim("type", "access")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact();
    }

    public String generateRefreshToken(String email, UUID userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(email)
            .claim("uid", userId.toString())
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact();
    }

    public String generateVaultToken(String email, UUID userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + VAULT_TOKEN_EXPIRATION_MS);

        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(email)
            .claim("uid", userId.toString())
            .claim("type", "vault")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isAccessTokenValid(String token, String expectedEmail) {
        try {
            Claims claims = parseClaims(token);
            String tokenType = claims.get("type", String.class);
            return "access".equals(tokenType)
                && expectedEmail.equalsIgnoreCase(claims.getSubject())
                && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isVaultTokenValid(String token, String expectedEmail) {
        if (token == null || token.isBlank() || expectedEmail == null) {
            return false;
        }
        try {
            Claims claims = parseClaims(token.trim());
            String tokenType = claims.get("type", String.class);
            return "vault".equals(tokenType)
                && expectedEmail.equalsIgnoreCase(claims.getSubject())
                && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}
