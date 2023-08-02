package com.msik404.karmaapp.auth;

import java.util.Optional;

import com.msik404.karmaapp.auth.dto.LoginRequest;
import com.msik404.karmaapp.auth.dto.LoginResponse;
import com.msik404.karmaapp.auth.dto.RegisterRequest;
import com.msik404.karmaapp.constraintExceptions.ConstraintExceptionsHandler;
import com.msik404.karmaapp.constraintExceptions.DataIntegrityViolationExceptionErrorMessageExtractionStrategy;
import com.msik404.karmaapp.constraintExceptions.DuplicateEmailException;
import com.msik404.karmaapp.constraintExceptions.RoundBraceErrorMassageParseStrategy;
import com.msik404.karmaapp.user.Role;
import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.UserRepository;
import com.msik404.karmaapp.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository repository;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ConstraintExceptionsHandler constraintExceptionsHandler;
    private final DataIntegrityViolationExceptionErrorMessageExtractionStrategy extractionStrategy;
    private final RoundBraceErrorMassageParseStrategy parseStrategy;

    public void register(RegisterRequest request) throws DuplicateEmailException {

        try {
            repository.save(User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(bCryptPasswordEncoder.encode(request.getPassword()))
                    .role(Role.USER)
                    // Nullable
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    // basic values
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true).
                    enabled(true)
                    .build());
        } catch (DataIntegrityViolationException ex) {
            constraintExceptionsHandler.handle(ex, extractionStrategy, parseStrategy);
        }
    }

    public LoginResponse login(LoginRequest request) throws AuthenticationException {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userService.findByUsername(request.getUsername());

        return new LoginResponse(jwtService.generateJwt(user, Optional.empty()));
    }

}
