package dev.sivalabs.reviewbuddy.products.domain.models;

public record CreateProductCmd(
        String name, String tagline, String description, String categorySlug, String url, String imageUrl) {}
