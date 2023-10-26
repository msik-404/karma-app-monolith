package com.msik404.karmaapp.exception.constraint.chain;

import com.msik404.karmaapp.exception.constraint.exception.DuplicateUnexpectedFieldException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class UndefinedConstraintExceptionHandler extends BaseExceptionHandler {

    private String getErrorMessage(@Nullable String fieldName, @Nullable String value) {

        String errorMessage = "Some constraint has been violated, but concrete reason is unknown";
        if (fieldName != null && value != null) {
            errorMessage = String.format("Some constraint on field: (%s) has been violated with value: (%s)", fieldName, value);
        }
        return errorMessage;
    }

    @Override
    public void handle(@NonNull String fieldName, @NonNull String errorMessage) throws RuntimeException {

        if (super.nextHandler.isEmpty()) {
            throw new DuplicateUnexpectedFieldException(getErrorMessage(fieldName, errorMessage));
        }
    }

}
