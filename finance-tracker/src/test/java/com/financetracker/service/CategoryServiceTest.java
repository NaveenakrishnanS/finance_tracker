package com.financetracker.service;

import com.financetracker.dto.category.CategoryRequest;
import com.financetracker.dto.category.CategoryResponse;
import com.financetracker.exception.DuplicateResourceException;
import com.financetracker.exception.ForbiddenOperationException;
import com.financetracker.exception.ReferentialIntegrityException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.CategoryMapper;
import com.financetracker.model.Category;
import com.financetracker.model.User;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CategoryService.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Category systemCat;
    private Category userCat;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        systemCat = new Category();
        systemCat.setId(1L);
        systemCat.setName("Food");
        systemCat.setSystemDefault(true);

        userCat = new Category();
        userCat.setId(2L);
        userCat.setName("MyCategory");
        userCat.setSystemDefault(false);
        userCat.setUser(user);
    }

    @Test
    void getCategories_returnsMergedListOfSystemDefaultsAndUserCategories() {
        when(categoryRepository.findByUserIsNullOrUserId(1L))
                .thenReturn(List.of(systemCat, userCat));
        when(categoryMapper.toResponse(systemCat))
                .thenReturn(new CategoryResponse(1L, "Food", true));
        when(categoryMapper.toResponse(userCat))
                .thenReturn(new CategoryResponse(2L, "MyCategory", false));

        var result = categoryService.getCategories(1L);
        assertThat(result).hasSize(2);
    }

    @Test
    void createCategory_successPath_persistsAndReturnsCategoryResponse() {
        when(categoryRepository.existsByNameAndUserId("NewCat", 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.save(any(Category.class))).thenReturn(userCat);
        when(categoryMapper.toResponse(userCat))
                .thenReturn(new CategoryResponse(2L, "MyCategory", false));

        var result = categoryService.createCategory(1L, new CategoryRequest("NewCat"));
        assertThat(result).isNotNull();
    }

    @Test
    void createCategory_throwsDuplicateResourceException_onDuplicateName() {
        when(categoryRepository.existsByNameAndUserId("Food", 1L)).thenReturn(true);
        assertThatThrownBy(() ->
                categoryService.createCategory(1L, new CategoryRequest("Food")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void deleteCategory_throwsForbiddenOperationException_forSystemDefaultCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(systemCat));
        assertThatThrownBy(() -> categoryService.deleteCategory(1L, 1L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void deleteCategory_throwsReferentialIntegrityException_whenCategoryIsReferenced() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(userCat));
        when(categoryRepository.countTransactionsByCategoryId(2L)).thenReturn(5L);
        when(categoryRepository.countBudgetsByCategoryId(2L)).thenReturn(0L);
        assertThatThrownBy(() -> categoryService.deleteCategory(1L, 2L))
                .isInstanceOf(ReferentialIntegrityException.class);
    }

    @Test
    void deleteCategory_successPath_deletesUnreferencedCustomCategory() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(userCat));
        when(categoryRepository.countTransactionsByCategoryId(2L)).thenReturn(0L);
        when(categoryRepository.countBudgetsByCategoryId(2L)).thenReturn(0L);
        categoryService.deleteCategory(1L, 2L);
        verify(categoryRepository).delete(userCat);
    }

    @Test
    void deleteCategory_throwsResourceNotFoundException_whenNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.deleteCategory(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
