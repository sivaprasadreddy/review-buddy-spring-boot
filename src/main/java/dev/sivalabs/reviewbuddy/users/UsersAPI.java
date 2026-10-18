package dev.sivalabs.reviewbuddy.users;

import dev.sivalabs.reviewbuddy.users.config.AuthUtils;
import dev.sivalabs.reviewbuddy.users.domain.UserService;
import dev.sivalabs.reviewbuddy.users.domain.models.UserDto;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UsersAPI {
    private final UserService userService;

    UsersAPI(UserService userService) {
        this.userService = userService;
    }

    public Optional<UserDto> findById(Long id) {
        return userService.findById(id);
    }

    public Optional<Long> getCurrentUserId() {
        return Optional.ofNullable(AuthUtils.getCurrentUserId());
    }
}
