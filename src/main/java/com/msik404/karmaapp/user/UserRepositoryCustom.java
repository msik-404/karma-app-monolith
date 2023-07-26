package com.msik404.karmaapp.user;

import com.msik404.karmaapp.user.dtos.UserDtoWithAdminPrivilege;
import com.msik404.karmaapp.user.dtos.UserDtoWithUserPrivilege;

public interface UserRepositoryCustom {

    void updateNonNull(Long userId, UserDtoWithUserPrivilege dto);

    void updateNonNull(Long userId, UserDtoWithAdminPrivilege dto);
    
}
