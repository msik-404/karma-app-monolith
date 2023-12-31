package com.msik404.karmaappmonolith.exception.constraint.strategy;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class DataIntegrityViolationExceptionErrorMessageExtractionStrategy implements Strategy<RuntimeException, String> {

    @NonNull
    @Override
    public String execute(@NonNull RuntimeException ex) {
        return ((DataIntegrityViolationException) ex).getMostSpecificCause().getMessage();
    }
}
