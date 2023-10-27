package com.msik404.karmaapp.post.dto;

import com.msik404.karmaapp.post.Visibility;
import org.springframework.lang.NonNull;

public record VisibilityOnlyDto(@NonNull Visibility visibility) {
}
