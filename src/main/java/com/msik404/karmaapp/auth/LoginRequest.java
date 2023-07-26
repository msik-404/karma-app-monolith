package com.msik404.karmaapp.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

    @NotNull private final String username;
    @NotNull private final String password;

}

