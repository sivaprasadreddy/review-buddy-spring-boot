package dev.sivalabs.reviewbuddy.users;

record CreateUserCmd(String name, String email, String password, Role role) {}
