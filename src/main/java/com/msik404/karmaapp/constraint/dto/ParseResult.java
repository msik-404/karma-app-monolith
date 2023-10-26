package com.msik404.karmaapp.constraint.dto;

import org.springframework.lang.NonNull;

public record ParseResult(@NonNull String fieldName, @NonNull String value) {
}
