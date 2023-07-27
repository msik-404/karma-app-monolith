package com.msik404.karmaapp.auth;

public class DuplicateEmailExeption extends Exception {
    public DuplicateEmailExeption() {
        super("This email is already used");
    }
}
