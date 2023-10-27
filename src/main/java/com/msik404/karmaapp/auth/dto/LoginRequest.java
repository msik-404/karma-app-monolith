package com.msik404.karmaapp.auth.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotNull String username, @NotNull String password) {
}
