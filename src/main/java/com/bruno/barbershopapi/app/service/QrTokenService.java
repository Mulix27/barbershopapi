package com.bruno.barbershopapi.app.service;

import com.bruno.barbershopapi.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class QrTokenService {

    @Value("${app.jwt.secret}")
    private String secret;

    // Token para subir fotos (barbero) — expira en 15 min
    public String generatePhotoToken(UUID clientHaircutId, UUID barbershopId) {
        return Jwts.builder()
                .subject("qr-photo")
                .claim("clientHaircutId", clientHaircutId.toString())
                .claim("barbershopId",    barbershopId.toString())
                .claim("type",            "photo")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 min
                .signWith(getKey())
                .compact();
    }

    // Token para agenda (cliente) — expira en 60 min
    public String generateAgendaToken(UUID barbershopId) {
        return Jwts.builder()
                .subject("qr-agenda")
                .claim("barbershopId", barbershopId.toString())
                .claim("type",         "agenda")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000)) // 60 min
                .signWith(getKey())
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isExpired(String token) {
        try {
            return validateToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}