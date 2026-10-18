package dev.sivalabs.reviewbuddy.users.config;

import dev.sivalabs.reviewbuddy.users.domain.UserService;
import dev.sivalabs.reviewbuddy.users.domain.models.UserCredentialsDto;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
class SecurityUserDetailsProvider implements UserDetailsService {
    private final UserService userService;

    SecurityUserDetailsProvider(UserService userService) {
        this.userService = userService;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String userName) {
        return userService
                .findCredentialsByEmail(userName)
                .map(this::toSecurityUser)
                .orElseThrow(() -> new UsernameNotFoundException("Email " + userName + " not found"));
    }

    private SecurityUser toSecurityUser(UserCredentialsDto user) {
        return new SecurityUser(user.id(), user.name(), user.email(), user.password(), user.role());
    }
}
