package com.badminton.news.dto;

import com.badminton.news.entity.ShopPost;
import jakarta.validation.constraints.*;

public record ShopPostRequest(
    @NotBlank @Size(max = 180) String title,
    @NotBlank @Size(max = 400) String summary,
    @NotBlank @Size(max = 30000) String content,
    @NotNull ShopPost.Category category,
    @NotNull ShopPost.Status status) {}
