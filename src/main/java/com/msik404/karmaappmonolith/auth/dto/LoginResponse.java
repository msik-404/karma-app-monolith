package com.msik404.karmaappmonolith.auth.dto;

import org.springframework.lang.NonNull;

public record LoginResponse(@NonNull String jwt) {
}
