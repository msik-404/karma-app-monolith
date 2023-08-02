package com.msik404.karmaapp.constraintExceptions;

import com.msik404.karmaapp.strategy.Strategy;
import org.springframework.stereotype.Component;

@Component
public class ConstraintViolationExceptionErrorMessageExtractionStrategy implements Strategy<RuntimeException, String> {

    @Override
    public String execute(RuntimeException ex) {
        return ex.getMessage();
    }
}
