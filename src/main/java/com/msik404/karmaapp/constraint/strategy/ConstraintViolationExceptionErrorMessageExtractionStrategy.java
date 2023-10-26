package com.msik404.karmaapp.constraint.strategy;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ConstraintViolationExceptionErrorMessageExtractionStrategy implements Strategy<RuntimeException, String> {

    @Override
    public String execute(@NonNull RuntimeException ex) {
        return ex.getMessage();
    }
}
