package com.msik404.karmaapp.user;

import com.msik404.karmaapp.constraintExceptions.DuplicateEmailException;
import com.msik404.karmaapp.constraintExceptions.DuplicateUsernameException;
import com.msik404.karmaapp.constraintExceptions.UndefinedConstraintException;
import com.msik404.karmaapp.user.dto.UserDtoWithUserPrivilege;
import org.springframework.lang.NonNull;

public interface UserRepositoryCustom {

    int updateNonNull(long userId, @NonNull UserDtoWithUserPrivilege dto)
            throws DuplicateEmailException, DuplicateUsernameException, UndefinedConstraintException;

}
