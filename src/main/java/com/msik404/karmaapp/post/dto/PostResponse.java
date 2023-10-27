package com.msik404.karmaapp.post.dto;

import com.msik404.karmaapp.post.Visibility;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record PostResponse(
        long id,
        @NonNull String username,
        @Nullable String headline,
        @Nullable String text,
        long karmaScore,
        @NonNull Visibility visibility) {
}
