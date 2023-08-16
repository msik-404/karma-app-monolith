package com.msik404.karmaapp.post.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostRatingResponse {

    private Long id;

    private Boolean wasRatedPositively;

}
