package com.msik404.karmaapp.constraintExceptions;

import com.msik404.karmaapp.pair.Pair;
import com.msik404.karmaapp.strategy.Strategy;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConstraintExceptionsHandler {

    public void handle(
            @NonNull RuntimeException ex,
            @NonNull Strategy<RuntimeException, String> extractionStrategy,
            @NonNull Strategy<String, Pair<String, String>> parseStrategy)
            throws DuplicateEmailException, DuplicateUsernameException, UndefinedConstraintException {

        String errorMessage = extractionStrategy.execute(ex);
        Pair<String, String> request = parseStrategy.execute(errorMessage);

        var emailExceptionHandler = new DuplicateEmailExceptionHandler();
        var usernameExceptionHandler = new DuplicateUsernameExceptionHandler();
        emailExceptionHandler.setNext(usernameExceptionHandler);
        usernameExceptionHandler.setNext(new UndefinedConstraintExceptionHandler());
        emailExceptionHandler.handle(request);
    }

}
