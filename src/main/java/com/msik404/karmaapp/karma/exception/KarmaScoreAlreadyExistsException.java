package com.msik404.karmaapp.karma.exception;

public class KarmaScoreAlreadyExistsException extends RuntimeException {

    public KarmaScoreAlreadyExistsException() {
        super("KarmaScore with that userId and postId already exists");
    }

    public KarmaScoreAlreadyExistsException(String message) {
        super(message);
    }

}
