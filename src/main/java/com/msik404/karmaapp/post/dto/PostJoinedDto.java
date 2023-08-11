package com.msik404.karmaapp.post.dto;

import com.msik404.karmaapp.post.PostVisibility;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostJoinedDto {

    private final Long id;

    private final Long userId;

    private final String username;

    private final String headline;

    private final String text;

    private final Long karmaScore;

    private PostVisibility visibility;

    // null means user is unauthenticated
    private final Boolean wasRatedByAuthenticatedUserPositively;

}
