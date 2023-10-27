package com.msik404.karmaapp.user.dto;

import jakarta.validation.constraints.Email;
import org.springframework.lang.Nullable;

public record UserUpdateRequestWithUserPrivilege(
        @Nullable String firstName,
        @Nullable String lastName,
        @Nullable String username,
        @Nullable @Email String email,
        @Nullable String password) {

}
