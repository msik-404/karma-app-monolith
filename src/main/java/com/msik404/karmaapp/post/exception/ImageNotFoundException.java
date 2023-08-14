package com.msik404.karmaapp.post.exception;

public class ImageNotFoundException extends RuntimeException {

    public ImageNotFoundException() {
        super("Requested image was not found");
    }
}
