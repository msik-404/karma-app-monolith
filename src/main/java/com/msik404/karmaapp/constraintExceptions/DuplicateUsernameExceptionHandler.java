package com.msik404.karmaapp.constraintExceptions;

import com.msik404.karmaapp.pair.Pair;
import org.springframework.lang.NonNull;

public class DuplicateUsernameExceptionHandler extends BaseExceptionHandler {

    @Override
    public void handle(@NonNull Pair<String, String> request) throws RuntimeException {

        if (request.first().equals("username")) {
            throw new DuplicateUsernameException(request.second());
        }
        super.nextHandler.ifPresent(handler -> handler.handle(request));
    }

}
