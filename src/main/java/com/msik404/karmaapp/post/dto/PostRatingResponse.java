package com.msik404.karmaapp.post.dto;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record PostRatingResponse(@NonNull long id, @Nullable Boolean wasRatedPositively) {
}
