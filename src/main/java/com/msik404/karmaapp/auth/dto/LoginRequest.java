package com.msik404.karmaapp.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

    @NotNull
    private final String username;
    @NotNull
    private final String password;

}

