package com.msik404.karmaapp.user.repository;

import com.msik404.karmaapp.exception.constraint.exception.DuplicateEmailException;
import com.msik404.karmaapp.exception.constraint.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaapp.exception.constraint.exception.DuplicateUsernameException;
import com.msik404.karmaapp.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaapp.user.dto.UserUpdateRequestWithUserPrivilege;
import com.msik404.karmaapp.user.exception.NoFieldSetException;
import org.springframework.lang.NonNull;

public interface UserRepositoryCustom {

    int updateNonNull(long userId, @NonNull UserUpdateRequestWithUserPrivilege dto)
            throws NoFieldSetException, DuplicateEmailException, DuplicateUsernameException, DuplicateUnexpectedFieldException;

    int updateNonNull(long userId, @NonNull UserUpdateRequestWithAdminPrivilege dto)
            throws NoFieldSetException, DuplicateEmailException, DuplicateUsernameException, DuplicateUnexpectedFieldException;

}
