package com.msik404.karmaapp.user;

import com.msik404.karmaapp.auth.DuplicateEmailException;
import com.msik404.karmaapp.user.dto.UserDtoWithAdminPrivilege;
import com.msik404.karmaapp.user.dto.UserDtoWithUserPrivilege;

public interface UserRepositoryCustom {

    void updateNonNull(Long userId, UserDtoWithUserPrivilege dto) throws DuplicateEmailException;

    void updateNonNull(Long userId, UserDtoWithAdminPrivilege dto) throws DuplicateEmailException;
    
}
