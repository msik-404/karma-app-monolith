package com.msik404.karmaappmonolith.auth;

import com.msik404.karmaappmonolith.auth.dto.LoginRequest;
import com.msik404.karmaappmonolith.auth.dto.LoginResponse;
import com.msik404.karmaappmonolith.auth.dto.RegisterRequest;
import com.msik404.karmaappmonolith.auth.jwt.JwtService;
import com.msik404.karmaappmonolith.exception.constraint.ConstraintExceptionsHandler;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateEmailException;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateUsernameException;
import com.msik404.karmaappmonolith.exception.constraint.strategy.DataIntegrityViolationExceptionErrorMessageExtractionStrategy;
import com.msik404.karmaappmonolith.exception.constraint.strategy.RoundBraceErrorMassageParseStrategy;
import com.msik404.karmaappmonolith.user.Role;
import com.msik404.karmaappmonolith.user.User;
import com.msik404.karmaappmonolith.user.UserService;
import com.msik404.karmaappmonolith.user.repository.UserRepository;
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
            repository.save(new User(
                    request.username(),
                    request.email(),
                    bCryptPasswordEncoder.encode(request.password()),
                    Role.USER,
                    request.firstName(),
                    request.lastName()
            ));
        } catch (DataIntegrityViolationException ex) {
            constraintExceptionsHandler.handle(ex, extractionStrategy, parseStrategy);
        }
    }

    public LoginResponse login(@NonNull LoginRequest request) throws AuthenticationException {

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        var user = (User) authentication.getPrincipal();

        return new LoginResponse(jwtService.generateJwt(user.getId(), null));
    }

}
