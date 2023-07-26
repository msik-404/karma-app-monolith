package com.msik404.karmaapp.auth;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    public String generateJwt(
            @NonNull UserDetails userDetails,
            Optional<Map<String, Object>> opt) {

        final Long currentTime = System.currentTimeMillis();
        // one hour
        final Long expirationTime = currentTime + 1000 * 60 * 60 * 1;

        JwtBuilder builder = Jwts.builder();
        opt.ifPresent(extraClaims -> builder.setClaims(extraClaims));
        return builder
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(expirationTime))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    public Claims extractAllClaims(String jwt) {

        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    public boolean isValid(Claims claims, UserDetails userDetails) {
        return (claims.getSubject().equals(userDetails.getUsername()) && !isTokenExpired(claims));
    }

    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
