package com.msik404.karmaapp.user.repository;

import com.msik404.karmaapp.exception.constraint.exception.DuplicateEmailException;
import com.msik404.karmaapp.exception.constraint.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaapp.exception.constraint.exception.DuplicateUsernameException;
import com.msik404.karmaapp.user.dto.UserUpdateRequestWithUserPrivilege;
import org.springframework.lang.NonNull;

public interface UserRepositoryCustom {

    int updateNonNull(long userId, @NonNull UserUpdateRequestWithUserPrivilege dto)
            throws DuplicateEmailException, DuplicateUsernameException, DuplicateUnexpectedFieldException;

}
