package com.msik404.karmaapp.post.dto;

import com.msik404.karmaapp.post.Visibility;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record PostWithImageDataDto(@NonNull PostDto postDto, @Nullable byte[] imageData) {

    public PostWithImageDataDto(
            long id,
            long userId,
            @NonNull String username,
            @Nullable String headline,
            @Nullable String text,
            long karmaScore,
            @NonNull Visibility visibility,
            @Nullable byte[] imageData) {

        this(new PostDto(id, userId, username, headline, text, karmaScore, visibility), imageData);
    }
}
