package dev.sivalabs.reviewbuddy.products.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

record SubmitProductRequest(
        @NotBlank @Size(max = 60) String name,
        @NotBlank @Size(max = 100) String tagline,
        @NotBlank @Size(max = 500) String description,
        @NotBlank String categorySlug,

        @NotBlank @Pattern(regexp = "https?://[^\\s/?#]+[^\\s]*", message = "Enter a valid HTTP or HTTPS URL")
        String url,

        @Pattern(regexp = "(?:https?://[^\\s/?#]+[^\\s]*)?", message = "Enter a valid HTTP or HTTPS URL")
        String imageUrl) {
    SubmitProductRequest {
        name = clean(name);
        tagline = clean(tagline);
        description = clean(description);
        categorySlug = clean(categorySlug);
        url = clean(url);
        imageUrl = clean(imageUrl);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
