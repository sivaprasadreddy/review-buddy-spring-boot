package dev.sivalabs.reviewbuddy.users.domain.models;

public record CreateUserCmd(String name, String email, String password, Role role) {}
