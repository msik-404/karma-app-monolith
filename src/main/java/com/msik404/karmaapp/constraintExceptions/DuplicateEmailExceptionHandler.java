package com.msik404.karmaapp.constraintExceptions;

import com.msik404.karmaapp.pair.Pair;
import org.springframework.lang.NonNull;

public class DuplicateEmailExceptionHandler extends BaseExceptionHandler {

    @Override
    public void handle(@NonNull Pair<String, String> request) throws RuntimeException {

        if (request.first().equals("email")) {
            throw new DuplicateEmailException(request.second());
        }
        super.nextHandler.ifPresent(handler -> handler.handle(request));
    }

}
