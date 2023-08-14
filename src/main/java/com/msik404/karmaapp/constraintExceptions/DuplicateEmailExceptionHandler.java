package com.msik404.karmaapp.constraintExceptions;

import org.springframework.lang.NonNull;

public class DuplicateEmailExceptionHandler extends BaseExceptionHandler {

    @Override
    public void handle(@NonNull String fieldName, @NonNull String errorMessage) throws RuntimeException {

        if (fieldName.equals("email")) {
            throw new DuplicateEmailException(errorMessage);
        }
        super.nextHandler.ifPresent(handler -> handler.handle(fieldName, errorMessage));
    }

}
