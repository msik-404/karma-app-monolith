package com.msik404.karmaapp.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

@RestControllerAdvice
public class AuthControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DisabledException.class)
    public ProblemDetail disabledException(DisabledException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Account is disabled");
    }

    @ExceptionHandler(LockedException.class)
    public ProblemDetail lockedException(LockedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Account is locked");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail badCredentialsException(BadCredentialsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials provided");
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ProblemDetail authenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Credentials were not provided");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accessDeniedException(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Access denied");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ProblemDetail expiredJwtException(ExpiredJwtException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "JWT is expired");
    }

    @ExceptionHandler(SignatureException.class)
    public ProblemDetail signatureException(SignatureException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "JWT signature is invalid");
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ProblemDetail malformedJwtException(MalformedJwtException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "JWT signature is malformed");
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail duplicateEmailExeption(DuplicateEmailException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "This email is already used");
    }

}
