package com.msik404.karmaapp.constraintExceptions;

import com.msik404.karmaapp.pair.Pair;

public class DuplicateEmailExceptionHandler extends BaseExceptionHandler {

    @Override
    public void handle(Pair<String, String> request) throws RuntimeException {

        if (request.first().equals("email")) {
            throw new DuplicateEmailException(request.second());
        }
        if (super.nextHandler != null) {
            super.nextHandler.handle(request);
        }
    }

}
