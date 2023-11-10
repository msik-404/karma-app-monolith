package com.msik404.karmaappmonolith.post.dto;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record PostRatingResponse(@NonNull long id, @Nullable Boolean wasRatedPositively) {
}
