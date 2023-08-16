package com.msik404.karmaapp.post.dto;

import com.msik404.karmaapp.post.PostVisibility;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostJoined {

    private Long id;

    private Long userId;

    private String username;

    private String headline;

    private String text;

    private Long karmaScore;

    private PostVisibility visibility;

}
