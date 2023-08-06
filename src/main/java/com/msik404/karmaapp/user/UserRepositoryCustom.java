package com.msik404.karmaapp.user;

import com.msik404.karmaapp.constraintExceptions.DuplicateEmailException;
import com.msik404.karmaapp.user.dto.UserDtoWithUserPrivilege;

public interface UserRepositoryCustom {

    void updateNonNull(long userId, UserDtoWithUserPrivilege dto) throws DuplicateEmailException, UserNotFoundException;

}
