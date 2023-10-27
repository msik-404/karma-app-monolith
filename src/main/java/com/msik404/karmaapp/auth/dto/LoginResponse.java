package com.msik404.karmaapp.auth.dto;

import org.springframework.lang.NonNull;

public record LoginResponse(@NonNull String jwt) {
}
