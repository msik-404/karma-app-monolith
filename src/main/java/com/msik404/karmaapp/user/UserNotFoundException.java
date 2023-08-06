package com.msik404.karmaapp.user;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("User with that id was not found");
    }

}
