package com.msik404.karmaapp.post;

public class ImageNotFoundException extends RuntimeException {

    public ImageNotFoundException() {
        super("Requested image was not found");
    }
}
