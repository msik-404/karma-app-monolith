package com.msik404.karmaapp.auth;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import com.msik404.karmaapp.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Generates new JWT.
     * Subject is set to user's Long type identifier which will be transformed to string.
     * @param user
     * @param opt
     * @return
     */
    public String generateJwt(
            @NonNull User user,
            Optional<Map<String, Object>> opt) {

        final long currentTime = System.currentTimeMillis();
        // one hour
        final long expirationTime = currentTime + 1000 * 60 * 60 * 1;

        JwtBuilder builder = Jwts.builder();
        opt.ifPresent(builder::setClaims);
        return builder
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(expirationTime))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    public Claims extractAllClaims(String jwt)
            throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException {

        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }
}
