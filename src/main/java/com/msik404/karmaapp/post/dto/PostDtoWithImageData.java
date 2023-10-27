package com.msik404.karmaapp.post.dto;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record PostDtoWithImageData(@NonNull PostDto postDto, @Nullable byte[] imageData) {
}
