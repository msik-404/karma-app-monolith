package com.msik404.karmaappmonolith.exception.constraint;

import com.msik404.karmaappmonolith.exception.constraint.chain.DuplicateEmailExceptionHandler;
import com.msik404.karmaappmonolith.exception.constraint.chain.DuplicateUsernameExceptionHandler;
import com.msik404.karmaappmonolith.exception.constraint.chain.UndefinedConstraintExceptionHandler;
import com.msik404.karmaappmonolith.exception.constraint.dto.ParseResult;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateEmailException;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateUsernameException;
import com.msik404.karmaappmonolith.exception.constraint.strategy.Strategy;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConstraintExceptionsHandler {

    public void handle(
            @NonNull RuntimeException ex,
            @NonNull Strategy<RuntimeException, String> extractionStrategy,
            @NonNull Strategy<String, ParseResult> parseStrategy)
            throws DuplicateEmailException, DuplicateUsernameException, DuplicateUnexpectedFieldException {

        String errorMessage = extractionStrategy.execute(ex);
        ParseResult parseResult = parseStrategy.execute(errorMessage);

        var emailExceptionHandler = new DuplicateEmailExceptionHandler();
        var usernameExceptionHandler = new DuplicateUsernameExceptionHandler();
        emailExceptionHandler.setNext(usernameExceptionHandler);
        usernameExceptionHandler.setNext(new UndefinedConstraintExceptionHandler());

        emailExceptionHandler.handle(parseResult.fieldName(), parseResult.value());
    }

}
