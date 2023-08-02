package com.msik404.karmaapp.constraintExceptions;

import com.msik404.karmaapp.strategy.Strategy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class DataIntegrityViolationExceptionErrorMessageExtractionStrategy implements Strategy<RuntimeException, String> {

    @Override
    public String execute(RuntimeException ex) {
        return ((DataIntegrityViolationException) ex).getMostSpecificCause().getMessage();
    }
}
