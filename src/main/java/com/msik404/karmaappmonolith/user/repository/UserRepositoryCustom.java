package com.msik404.karmaappmonolith.user.repository;

import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateEmailException;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateUsernameException;
import com.msik404.karmaappmonolith.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaappmonolith.user.dto.UserUpdateRequestWithUserPrivilege;
import com.msik404.karmaappmonolith.user.exception.NoFieldSetException;
import org.springframework.lang.NonNull;

public interface UserRepositoryCustom {

    int updateNonNull(long userId, @NonNull UserUpdateRequestWithUserPrivilege dto)
            throws NoFieldSetException, DuplicateEmailException, DuplicateUsernameException, DuplicateUnexpectedFieldException;

    int updateNonNull(long userId, @NonNull UserUpdateRequestWithAdminPrivilege dto)
            throws NoFieldSetException, DuplicateEmailException, DuplicateUsernameException, DuplicateUnexpectedFieldException;

}
