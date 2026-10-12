package com.financetracker.service;

import com.financetracker.dto.transaction.TransactionFilterParams;
import com.financetracker.dto.transaction.TransactionRequest;
import com.financetracker.dto.transaction.TransactionResponse;
import com.financetracker.exception.ForbiddenOperationException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.TransactionMapper;
import com.financetracker.model.Transaction;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing financial transactions.
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;

    /**
     * Constructs TransactionService with required dependencies.
     *
     * @param transactionRepository the transaction data repository
     * @param categoryRepository the category data repository
     * @param userRepository the user data repository
     * @param transactionMapper the transaction entity to DTO mapper
     */
    public TransactionService(TransactionRepository transactionRepository,
                               CategoryRepository categoryRepository,
                               UserRepository userRepository,
                               TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionMapper = transactionMapper;
    }

    /**
     * Creates a new transaction for the given user.
     *
     * @param userId the ID of the user creating the transaction
     * @param request the transaction creation request
     * @return the created transaction response
     * @throws ResourceNotFoundException if the user or category does not exist
     */
    @Transactional
    public TransactionResponse createTransaction(Long userId, TransactionRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category",
                        request.categoryId()));
        var transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setCategory(category);
        transaction.setDate(request.date());
        transaction.setDescription(request.description());
        transaction.setCreatedAt(LocalDateTime.now());
        var saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    /**
     * Returns a paginated, filtered list of transactions for the given user.
     *
     * @param userId the user ID
     * @param filters optional filter parameters
     * @param pageable pagination parameters
     * @return paginated page of transaction responses
     */
    public Page<TransactionResponse> getTransactions(Long userId,
                                                      TransactionFilterParams filters,
                                                      Pageable pageable) {
        return transactionRepository.findWithFilters(userId,
                filters.type(), filters.categoryId(),
                filters.startDate(), filters.endDate(), pageable)
                .map(transactionMapper::toResponse);
    }

    /**
     * Returns a single transaction by ID, enforcing ownership.
     *
     * @param userId the ID of the requesting user
     * @param id the transaction ID
     * @return the transaction response
     * @throws ResourceNotFoundException if the transaction does not exist
     * @throws ForbiddenOperationException if the transaction belongs to another user
     */
    public TransactionResponse getTransaction(Long userId, Long id) {
        var transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        if (!transaction.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "You do not have permission to access this transaction.");
        }
        return transactionMapper.toResponse(transaction);
    }

    /**
     * Updates an existing transaction.
     *
     * @param userId the ID of the requesting user
     * @param id the transaction ID to update
     * @param request the update request
     * @return the updated transaction response
     * @throws ResourceNotFoundException if the transaction or category does not exist
     * @throws ForbiddenOperationException if the transaction belongs to another user
     */
    @Transactional
    public TransactionResponse updateTransaction(Long userId, Long id,
                                                  TransactionRequest request) {
        var transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        if (!transaction.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "You do not have permission to update this transaction.");
        }
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category",
                        request.categoryId()));
        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setCategory(category);
        transaction.setDate(request.date());
        transaction.setDescription(request.description());
        var saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    /**
     * Deletes a transaction by ID, enforcing ownership.
     *
     * @param userId the ID of the requesting user
     * @param id the transaction ID to delete
     * @throws ResourceNotFoundException if the transaction does not exist
     * @throws ForbiddenOperationException if the transaction belongs to another user
     */
    @Transactional
    public void deleteTransaction(Long userId, Long id) {
        var transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        if (!transaction.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "You do not have permission to delete this transaction.");
        }
        transactionRepository.delete(transaction);
    }
}
