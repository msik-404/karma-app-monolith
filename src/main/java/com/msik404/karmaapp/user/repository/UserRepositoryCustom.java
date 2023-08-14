package com.msik404.karmaapp.user.repository;

import com.msik404.karmaapp.constraintExceptions.exception.DuplicateEmailException;
import com.msik404.karmaapp.constraintExceptions.exception.DuplicateUsernameException;
import com.msik404.karmaapp.constraintExceptions.exception.UndefinedConstraintException;
import com.msik404.karmaapp.user.dto.UserDtoWithUserPrivilege;
import org.springframework.lang.NonNull;

public interface UserRepositoryCustom {

    int updateNonNull(long userId, @NonNull UserDtoWithUserPrivilege dto)
            throws DuplicateEmailException, DuplicateUsernameException, UndefinedConstraintException;

}
