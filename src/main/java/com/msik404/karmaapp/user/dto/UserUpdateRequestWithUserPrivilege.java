package com.msik404.karmaapp.user.dto;

import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

@Data
@SuperBuilder
@NoArgsConstructor
public class UserUpdateRequestWithUserPrivilege {

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    @Nullable
    private String username;

    @Email
    @Nullable
    private String email;

    @Nullable
    private String password;

}
