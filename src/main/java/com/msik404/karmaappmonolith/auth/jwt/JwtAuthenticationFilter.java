package com.msik404.karmaappmonolith.auth.jwt;

import java.io.IOException;

import com.msik404.karmaappmonolith.user.User;
import com.msik404.karmaappmonolith.user.UserService;
import com.msik404.karmaappmonolith.user.exception.UserNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException, AuthenticationException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = authHeader.substring(TOKEN_PREFIX.length());
        try {
            Claims claims = jwtService.extractAllClaims(jwt);
            // 1. Get subject from token claims, null if something is wrong
            // 2. Checks whether user is not already authenticated,
            // this is useful when there are many authentication methods.
            if (claims.getSubject() != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // parse userId Long type represented as string to Long type
                final long userId = Long.parseLong(claims.getSubject());
                User user = userService.findById(userId);
                var authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        user.getAuthorities()
                );
                // Adds interesting data like ip address and session id
                authentication.setDetails(new WebAuthenticationDetails(request));
                // Docs state that this is required for thread safety:
                // https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            }
            filterChain.doFilter(request, response);

        } catch (UserNotFoundException ex) {
            throw new UsernameNotFoundException(ex.getMessage());
        } catch (ExpiredJwtException ex) {
            throw new CredentialsExpiredException(ex.getMessage());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException(ex.getMessage());
        }
    }
}
