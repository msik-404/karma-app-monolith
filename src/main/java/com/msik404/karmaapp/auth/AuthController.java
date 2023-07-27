package com.msik404.karmaapp.auth;

import javax.naming.AuthenticationException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.msik404.karmaapp.auth.dto.LoginRequest;
import com.msik404.karmaapp.auth.dto.LoginResponse;
import com.msik404.karmaapp.auth.dto.RegisterRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request)
            throws DuplicateEmailException {

        authenticationService.register(request);

        return ResponseEntity.ok(null);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request)
            throws AuthenticationException, UsernameNotFoundException {

        return ResponseEntity.ok(authenticationService.login(request));
    }

}
