package com.msik404.karmaapp.auth;

public class DuplicateEmailException extends Exception {
    public DuplicateEmailException() {
        super("This email is already used");
    }
}
