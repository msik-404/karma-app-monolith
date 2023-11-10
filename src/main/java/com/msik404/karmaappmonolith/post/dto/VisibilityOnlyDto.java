package com.msik404.karmaappmonolith.post.dto;

import com.msik404.karmaappmonolith.post.Visibility;
import org.springframework.lang.NonNull;

public record VisibilityOnlyDto(@NonNull Visibility visibility) {
}
