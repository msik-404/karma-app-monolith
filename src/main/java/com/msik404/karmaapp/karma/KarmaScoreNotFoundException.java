package com.msik404.karmaapp.karma;

public class KarmaScoreNotFoundException extends RuntimeException {

    public KarmaScoreNotFoundException() {
        super("KarmaScore with that id was not found");
    }

}
