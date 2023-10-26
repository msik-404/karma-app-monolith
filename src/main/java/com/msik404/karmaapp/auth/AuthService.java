package com.msik404.karmaapp.auth;

import java.util.Optional;

import com.msik404.karmaapp.auth.dto.LoginRequest;
import com.msik404.karmaapp.auth.dto.LoginResponse;
import com.msik404.karmaapp.auth.dto.RegisterRequest;
import com.msik404.karmaapp.auth.jwt.JwtService;
import com.msik404.karmaapp.constraint.ConstraintExceptionsHandler;
import com.msik404.karmaapp.constraint.exception.DuplicateEmailException;
import com.msik404.karmaapp.constraint.exception.DuplicateUsernameException;
import com.msik404.karmaapp.constraint.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaapp.constraint.strategy.DataIntegrityViolationExceptionErrorMessageExtractionStrategy;
import com.msik404.karmaapp.constraint.strategy.RoundBraceErrorMassageParseStrategy;
import com.msik404.karmaapp.user.Role;
import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.UserService;
import com.msik404.karmaapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
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

    public void register(@NonNull RegisterRequest request)
            throws DuplicateEmailException, DuplicateUsernameException, DuplicateUnexpectedFieldException {

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
                    .credentialsNonExpired(true)
                    .enabled(true)
                    .build());
        } catch (DataIntegrityViolationException ex) {
            constraintExceptionsHandler.handle(ex, extractionStrategy, parseStrategy);
        }
    }

    public LoginResponse login(@NonNull LoginRequest request) throws AuthenticationException {

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        var user = (User) authentication.getPrincipal();

        return new LoginResponse(jwtService.generateJwt(user, Optional.empty()));
    }

}
