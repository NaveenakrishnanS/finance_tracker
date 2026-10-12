package com.financetracker.service;

import com.financetracker.dto.category.CategoryRequest;
import com.financetracker.dto.category.CategoryResponse;
import com.financetracker.exception.DuplicateResourceException;
import com.financetracker.exception.ForbiddenOperationException;
import com.financetracker.exception.ReferentialIntegrityException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.CategoryMapper;
import com.financetracker.model.Category;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing transaction categories.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Constructs CategoryService with required dependencies.
     *
     * @param categoryRepository the category data repository
     * @param userRepository the user data repository
     * @param categoryMapper the category entity to DTO mapper
     */
    public CategoryService(CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.categoryMapper = categoryMapper;
    }

    /**
     * Returns all categories available to the user (system defaults and user-owned).
     *
     * @param userId the ID of the requesting user
     * @return list of available category responses
     */
    public List<CategoryResponse> getCategories(Long userId) {
        return categoryRepository.findByUserIsNullOrUserId(userId)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    /**
     * Creates a new user-defined category.
     *
     * @param userId the ID of the user creating the category
     * @param request the category creation request
     * @return the created category response
     * @throws DuplicateResourceException if a category with the same name already exists for the user
     */
    @Transactional
    public CategoryResponse createCategory(Long userId, CategoryRequest request) {
        if (categoryRepository.existsByNameAndUserId(request.name(), userId)) {
            throw new DuplicateResourceException(
                    "Category already exists with name: " + request.name());
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        var category = new Category();
        category.setName(request.name());
        category.setSystemDefault(false);
        category.setUser(user);
        var saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    /**
     * Deletes a user-owned category if it has no referencing transactions or budgets.
     *
     * @param userId the ID of the user requesting deletion
     * @param categoryId the ID of the category to delete
     * @throws ResourceNotFoundException if the category does not exist
     * @throws ForbiddenOperationException if the category is a system default
     * @throws ReferentialIntegrityException if the category is referenced by transactions or budgets
     */
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        var category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
        if (category.isSystemDefault()) {
            throw new ForbiddenOperationException(
                    "System-default categories cannot be deleted.");
        }
        long txCount = categoryRepository.countTransactionsByCategoryId(categoryId);
        long budgetCount = categoryRepository.countBudgetsByCategoryId(categoryId);
        if (txCount > 0 || budgetCount > 0) {
            throw new ReferentialIntegrityException(
                    "Cannot delete category: it is referenced by existing transactions or budgets.");
        }
        categoryRepository.delete(category);
    }
}
