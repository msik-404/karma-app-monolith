package com.msik404.karmaapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import com.msik404.karmaapp.auth.JwtAuthenticationFilter;
import com.msik404.karmaapp.user.Role;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final DelegatedAuthenticationEntryPoint entryPoint;

    @Bean
    public static RoleHierarchy roleHierarchy() {

        var hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_MOD\nROLE_MOD > ROLE_USER\n");
        return hierarchy;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        // I disabled many of the filters, because I don't like to have
        // things that I won't use
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session.disable())
                .securityContext(context -> context.disable()) // This is for securityContext persistence
                .anonymous(anonymous -> anonymous.disable())
                .headers(headers -> headers.disable())
                .requestCache(cache -> cache.disable())

                .exceptionHandling(handler -> handler.authenticationEntryPoint(entryPoint))

                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/admin/**").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers("/mod/**").hasAuthority(Role.MOD.toString())
                        .requestMatchers("/user/**").hasAuthority(Role.USER.toString())
                        .requestMatchers("/register", "/login", "/static/**", "guest/**").permitAll()
                        .anyRequest().authenticated())

                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, AuthorizationFilter.class)
                .build();
    }

}
