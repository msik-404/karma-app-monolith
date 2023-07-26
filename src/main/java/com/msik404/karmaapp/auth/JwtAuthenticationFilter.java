package com.msik404.karmaapp.auth;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.UserService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final static String tokenPrefix = "Bearer ";

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(tokenPrefix)) {
            filterChain.doFilter(request, response);
            return;
        }
        final String jwt = authHeader.substring(tokenPrefix.length());
        final Claims claims = jwtService.extractAllClaims(jwt);
        // Get username from token claims, null if something is wrong
        final String username = claims.getSubject();
        // Checks whether user is not already authenticated,
        // this is useful when there are many authentication methods.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                User user = userService.findByEmail(username);
                var authentication = new UsernamePasswordAuthenticationToken(user.getId(), null,
                        user.getAuthorities());
                // Adds interesting data like ip address and session id
                authentication.setDetails(new WebAuthenticationDetails(request));
                // Docs state that this is required for thread safety:
                // https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (UsernameNotFoundException e) {
                throw new AccessDeniedException("Access denied");
            }
        }
        filterChain.doFilter(request, response);
    }

}
