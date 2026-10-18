package dev.sivalabs.reviewbuddy.users.domain;

import dev.sivalabs.reviewbuddy.users.domain.exceptions.UserNotFoundException;
import dev.sivalabs.reviewbuddy.users.domain.models.CreateUserCmd;
import dev.sivalabs.reviewbuddy.users.domain.models.UpdateUserCmd;
import dev.sivalabs.reviewbuddy.users.domain.models.UserCredentialsDto;
import dev.sivalabs.reviewbuddy.users.domain.models.UserDto;
import java.time.Instant;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Optional<UserCredentialsDto> findCredentialsByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).map(this::toUserCredentialsDto);
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id).map(this::toUserDto);
    }

    @Transactional
    public void createUser(CreateUserCmd cmd) {
        var user = new UserEntity();
        user.setName(cmd.name());
        user.setEmail(cmd.email());
        user.setPassword(passwordEncoder.encode(cmd.password()));
        user.setRole(cmd.role());
        user.setCreatedAt(Instant.now());
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(Long userId, UpdateUserCmd cmd) {
        var user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.updateUser(user.getId(), cmd.name());
    }

    private UserDto toUserDto(UserEntity user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }

    private UserCredentialsDto toUserCredentialsDto(UserEntity user) {
        return new UserCredentialsDto(
                user.getId(), user.getName(), user.getEmail(), user.getPassword(), user.getRole());
    }
}
