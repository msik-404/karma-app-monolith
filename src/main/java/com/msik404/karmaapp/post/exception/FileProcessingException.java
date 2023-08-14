package com.msik404.karmaapp.post.exception;

public class FileProcessingException extends RuntimeException {

    public FileProcessingException() {
        super("File could not be processed for some reason");
    }
}
