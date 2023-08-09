package com.msik404.karmaapp.post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostCreationRequest {

    @NotNull
    private String headline;

    @NotNull
    private String text;

}
