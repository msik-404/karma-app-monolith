package com.msik404.karmaapp.post.dto;

import com.msik404.karmaapp.post.Visibility;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostDto {

    private Long id;

    private Long userId;

    private String username;

    private String headline;

    private String text;

    private Long karmaScore;

    private Visibility visibility;

}