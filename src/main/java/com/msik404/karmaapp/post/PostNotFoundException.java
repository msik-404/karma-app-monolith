package com.msik404.karmaapp.post;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException() {
        super("Post with that id was not found");
    }
}
