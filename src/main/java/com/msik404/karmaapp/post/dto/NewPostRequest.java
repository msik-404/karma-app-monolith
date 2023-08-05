package com.msik404.karmaapp.post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewPostRequest {

    @NotNull
    private String text;

}
