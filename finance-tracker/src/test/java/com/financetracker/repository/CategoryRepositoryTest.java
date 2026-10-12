package com.financetracker.repository;

import com.financetracker.model.Category;
import com.financetracker.model.User;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DataJpaTest for CategoryRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("cat-test@example.com");
        user.setPasswordHash("hash");
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);
    }

    private Category systemCategory(String name) {
        var c = new Category();
        c.setName(name);
        c.setSystemDefault(true);
        return c;
    }

    private Category userCategory(String name) {
        var c = new Category();
        c.setName(name);
        c.setSystemDefault(false);
        c.setUser(user);
        return c;
    }

    @Test
    void findByUserIsNullOrUserId_returnsSystemDefaultsWhenNoUserCategories() {
        categoryRepository.save(systemCategory("Food"));
        List<Category> result = categoryRepository.findByUserIsNullOrUserId(user.getId());
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(c -> c.isSystemDefault() || c.getUser().getId().equals(user.getId()));
    }

    @Test
    void findByUserIsNullOrUserId_returnsSystemDefaultsAndUserCategories() {
        categoryRepository.save(systemCategory("Food"));
        categoryRepository.save(userCategory("MyCategory"));
        List<Category> result = categoryRepository.findByUserIsNullOrUserId(user.getId());
        assertThat(result.stream().anyMatch(c -> c.isSystemDefault())).isTrue();
        assertThat(result.stream().anyMatch(c -> !c.isSystemDefault())).isTrue();
    }

    @Test
    void existsByNameAndUserId_returnsTrue_forDuplicateName() {
        categoryRepository.save(userCategory("MyCategory"));
        assertThat(categoryRepository.existsByNameAndUserId("MyCategory", user.getId())).isTrue();
    }

    @Test
    void existsByNameAndUserId_returnsFalse_forUniqueName() {
        assertThat(categoryRepository.existsByNameAndUserId("UniqueName", user.getId())).isFalse();
    }
}
