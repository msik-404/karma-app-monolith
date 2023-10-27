package com.msik404.karmaapp.post.dto;

import org.springframework.lang.NonNull;

public record ImageOnlyDto(@NonNull byte[] imageData) {
}
