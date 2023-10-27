package com.msik404.karmaapp.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.msik404.karmaapp.deserializer.UserUpdateRequestWithAdminPrivilegeDeserializer;
import com.msik404.karmaapp.user.Role;
import org.springframework.lang.Nullable;

@JsonDeserialize(using = UserUpdateRequestWithAdminPrivilegeDeserializer.class)
public record UserUpdateRequestWithAdminPrivilege(
        @Nullable UserUpdateRequestWithUserPrivilege userUpdateByUser,
        @Nullable Role role) {
}
