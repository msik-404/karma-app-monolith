package com.msik404.karmaapp.post.dto;

import com.msik404.karmaapp.post.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
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
