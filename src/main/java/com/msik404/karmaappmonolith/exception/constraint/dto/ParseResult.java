package com.msik404.karmaappmonolith.exception.constraint.dto;

import org.springframework.lang.NonNull;

public record ParseResult(@NonNull String fieldName, @NonNull String value) {
}
