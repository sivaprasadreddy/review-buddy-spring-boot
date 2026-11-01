package dev.sivalabs.reviewbuddy.products.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record SubmitReviewRequest(
        @NotBlank @Size(max = 400) String liked,
        @NotBlank @Size(max = 400) String improvements) {}
