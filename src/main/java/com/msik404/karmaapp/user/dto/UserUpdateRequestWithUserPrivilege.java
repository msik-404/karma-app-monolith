package com.msik404.karmaapp.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
@Setter
@Getter
public class UserUpdateRequestWithUserPrivilege {

    private final String firstName;
    private final String lastName;

    private final String username;
    @Email
    private final String email;
    private final String password;

}
