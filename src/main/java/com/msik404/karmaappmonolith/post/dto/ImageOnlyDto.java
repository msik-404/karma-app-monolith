package com.msik404.karmaappmonolith.post.dto;

import org.springframework.lang.Nullable;

public record ImageOnlyDto(@Nullable byte[] imageData) {
}
