package com.msik404.karmaapp.post.dto;

import com.msik404.karmaapp.post.Visibility;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

@SuperBuilder
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class PostDtoWithImageData extends PostDto {

    private byte[] imageData;

    public PostDtoWithImageData(
            @Nullable Long id,
            @Nullable Long userId,
            @Nullable String username,
            @Nullable String headline,
            @Nullable String text,
            @Nullable Long karmaScore,
            @Nullable Visibility visibility,
            @Nullable byte[] imageData) {

        super(id, userId, username, headline, text, karmaScore, visibility);

        this.imageData = imageData;
    }

}
