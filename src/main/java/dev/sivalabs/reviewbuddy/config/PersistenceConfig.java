package dev.sivalabs.reviewbuddy.config;

import dev.sivalabs.reviewbuddy.users.UsersAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
class PersistenceConfig {

    @Bean
    AuditorAware<Long> auditorProvider(UsersAPI usersAPI) {
        return usersAPI::getCurrentUserId;
    }
}
