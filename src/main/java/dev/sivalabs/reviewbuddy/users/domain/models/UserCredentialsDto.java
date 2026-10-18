package dev.sivalabs.reviewbuddy.users.domain.models;

public record UserCredentialsDto(Long id, String name, String email, String password, Role role) {}
