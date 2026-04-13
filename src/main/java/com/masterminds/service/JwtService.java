package com.masterminds.service;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class JwtService {
	
	@Value("${jwt.token.secret.key}")
	private String secretString;
	
	private SecretKey secretKey;

    // This runs automatically after the service is created and the @Value is injected
    @PostConstruct
    protected void init() {
    	System.out.println("secretString: " + secretString);
        this.secretKey = Keys.hmacShaKeyFor(secretString.getBytes());
    }

    public String generateToken(String phoneNumber, UUID userId) {
        return Jwts.builder()
                .subject(phoneNumber)
                .claim("userId", userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 864000000)) // 10 days expiry
                .signWith(secretKey)
                .compact();
    }
    
    public String extractPhoneNumber(String token) {
        return Jwts.parser()
                .verifyWith(secretKey) // The dynamic key we set up earlier
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Date expiration = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
        	e.printStackTrace();
            return false;
        }
    }
	
}
