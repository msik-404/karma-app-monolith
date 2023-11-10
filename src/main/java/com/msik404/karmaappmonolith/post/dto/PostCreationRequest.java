package com.msik404.karmaappmonolith.post.dto;

import jakarta.validation.constraints.NotNull;

public record PostCreationRequest(@NotNull String headline, @NotNull String text) {
}
