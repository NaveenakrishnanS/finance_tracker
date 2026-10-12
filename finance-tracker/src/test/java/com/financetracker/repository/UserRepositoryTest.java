package com.financetracker.repository;

import com.financetracker.model.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DataJpaTest for UserRepository verifying database-level behaviour.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User buildUser(String email) {
        var user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed-password");
        user.setDisplayName("Test User");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    @Test
    void findByEmail_returnsUser_whenEmailExists() {
        userRepository.save(buildUser("test@example.com"));
        var result = userRepository.findByEmail("test@example.com");
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByEmail_returnsEmpty_whenEmailDoesNotExist() {
        var result = userRepository.findByEmail("notfound@example.com");
        assertThat(result).isEmpty();
    }

    @Test
    void save_throwsDataIntegrityViolationException_forDuplicateEmail() {
        userRepository.save(buildUser("dup@example.com"));
        assertThatThrownBy(() -> {
            userRepository.saveAndFlush(buildUser("dup@example.com"));
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_persistsCreatedAt() {
        var before = LocalDateTime.now().minusSeconds(1);
        var saved = userRepository.save(buildUser("time@example.com"));
        assertThat(saved.getCreatedAt()).isAfter(before);
    }
}
