package dev.sivalabs.reviewbuddy.products.domain.models;

import java.time.Instant;

public record ProductDto(
        Long id,
        String name,
        String tagline,
        String description,
        CategoryDto category,
        String maker,
        String url,
        String imageUrl,
        Instant createdAt) {}
