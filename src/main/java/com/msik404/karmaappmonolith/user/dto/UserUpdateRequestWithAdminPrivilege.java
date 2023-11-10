package com.msik404.karmaappmonolith.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.msik404.karmaappmonolith.deserializer.UserUpdateRequestWithAdminPrivilegeDeserializer;
import com.msik404.karmaappmonolith.user.Role;
import org.springframework.lang.Nullable;

@JsonDeserialize(using = UserUpdateRequestWithAdminPrivilegeDeserializer.class)
public record UserUpdateRequestWithAdminPrivilege(
        @Nullable UserUpdateRequestWithUserPrivilege userUpdateByUser,
        @Nullable Role role) {
}
