package com.msik404.karmaapp.constraintExceptions;

import org.springframework.lang.NonNull;

public class DuplicateUsernameExceptionHandler extends BaseExceptionHandler {

    @Override
    public void handle(@NonNull String fieldName, @NonNull String errorMessage) throws RuntimeException {

        if (fieldName.equals("username")) {
            throw new DuplicateUsernameException(errorMessage);
        }
        super.nextHandler.ifPresent(handler -> handler.handle(fieldName, errorMessage));
    }

}
