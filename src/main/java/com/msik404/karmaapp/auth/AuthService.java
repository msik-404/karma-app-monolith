package com.msik404.karmaapp.auth;

import java.util.Optional;

import javax.naming.AuthenticationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.msik404.karmaapp.auth.dto.LoginRequest;
import com.msik404.karmaapp.auth.dto.LoginResponse;
import com.msik404.karmaapp.auth.dto.RegisterRequest;
import com.msik404.karmaapp.user.Role;
import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.UserRepository;
import com.msik404.karmaapp.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository repository;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public void register(RegisterRequest request)
            throws DuplicateEmailExeption {

        try {
            repository.save(User.builder()
                    .email(request.getEmail())
                    .password(bCryptPasswordEncoder.encode(request.getPassword()))
                    .role(Role.USER)
                    // Nullable
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    // basic values
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .enabled(true)
                    .build());
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailExeption();
        }
    }

    public LoginResponse login(LoginRequest request)
            throws AuthenticationException, UsernameNotFoundException {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(), request.getPassword()));

        UserDetails user = userService.findByEmail(request.getUsername());

        return new LoginResponse(jwtService.generateJwt(user, Optional.empty()));
    }

}
