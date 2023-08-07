package com.msik404.karmaapp.post.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostResponse {

    private final Long id;

    private final String text;

    private final Long karmaScore;

    private final String username;
}
