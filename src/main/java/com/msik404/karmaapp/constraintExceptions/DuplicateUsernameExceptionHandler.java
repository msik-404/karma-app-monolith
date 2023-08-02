package com.msik404.karmaapp.constraintExceptions;

import com.msik404.karmaapp.pair.Pair;

public class DuplicateUsernameExceptionHandler extends BaseExceptionHandler {

    @Override
    public void handle(Pair<String, String> request) throws RuntimeException {

        if (request.first().equals("username")) {
            throw new DuplicateUsernameException(request.second());
        }
        if (super.nextHandler != null) {
            super.nextHandler.handle(request);
        }
    }

}
