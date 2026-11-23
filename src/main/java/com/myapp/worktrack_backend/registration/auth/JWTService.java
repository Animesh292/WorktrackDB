package com.myapp.worktrack_backend.registration.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JWTService {
    @Value("${jwt.secret}")
    private String secret;

    private static final long EXPIRATION_MILLIS = 2 * 60 * 60 * 1000;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long employeeId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime()+EXPIRATION_MILLIS);

        return Jwts.builder().setSubject(String.valueOf(employeeId)).setIssuedAt(now).setExpiration(expiry).signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    public String validateAndExtract(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().getSubject();
    }
}
