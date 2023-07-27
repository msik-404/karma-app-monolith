package com.msik404.karmaapp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotNull @Email private final String email;
    @NotNull private final String password;

    private final String firstName;
    private final String lastName;
}
