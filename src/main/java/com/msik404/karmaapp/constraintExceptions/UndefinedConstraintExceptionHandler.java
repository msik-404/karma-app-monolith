package com.msik404.karmaapp.constraintExceptions;

import com.msik404.karmaapp.pair.Pair;

public class UndefinedConstraintExceptionHandler extends BaseExceptionHandler {

    private String getErrorMessage(String fieldName, String value) {

        String errorMessage = "Some constraint has been violated, but concrete reason is unknown";
        if (fieldName != null && value != null) {
            errorMessage = String.format("Some constraint on field: (%s) has been violated with value: (%s)", fieldName, value);
        }
        return errorMessage;
    }

    @Override
    public void handle(Pair<String, String> request) throws RuntimeException {

        if (super.nextHandler == null) {
            throw new UndefinedConstraintException(getErrorMessage(request.first(), request.second()));
        }
    }

}
