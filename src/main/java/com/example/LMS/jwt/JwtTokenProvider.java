package com.example.LMS.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${jwt.refreshExpiration}")
    private long refreshTokenExpirationInMs;

    // Token hosil qilish
    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationInMs); // longer
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Generate Access Token from Username (for refresh flow)
    public String generateAccessTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    // Username ni tokendan olish
    public String getUsernameFromJWT(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // Tokenni tekshirish
    public boolean validateToken(String authToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            // Log a signature exception
        } catch (MalformedJwtException ex) {
            // Log a malformed JWT exception
        } catch (ExpiredJwtException ex) {
            // Log an expired JWT exception
        } catch (UnsupportedJwtException ex) {
            // Log an unsupported JWT exception
        } catch (IllegalArgumentException ex) {
            // Log an illegal argument exception
        }
        return false;
    }
}
