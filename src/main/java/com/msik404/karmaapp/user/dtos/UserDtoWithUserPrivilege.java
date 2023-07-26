package com.msik404.karmaapp.user.dtos;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor @ToString @Setter @Getter
public class UserDtoWithUserPrivilege {

    private final String firstName;
    private final String lastName;

    @Email
    private final String email;
    private final String password;

}
