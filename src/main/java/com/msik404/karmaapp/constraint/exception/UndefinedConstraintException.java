package com.msik404.karmaapp.constraint.exception;

// todo: change name of this class to DuplicateUnexpectedFieldException
public class UndefinedConstraintException extends AbstractDuplicateFieldRestException {

    public UndefinedConstraintException(String errorMessage) {
        super(errorMessage);
    }

}
