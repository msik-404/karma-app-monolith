package com.msik404.karmaapp.post;

public class FileProcessingException extends RuntimeException {

    public FileProcessingException() {
        super("File could not be processed for some reason");
    }
}
