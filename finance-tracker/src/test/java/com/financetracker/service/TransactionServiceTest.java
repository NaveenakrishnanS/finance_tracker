package com.financetracker.service;

import com.financetracker.dto.transaction.TransactionFilterParams;
import com.financetracker.dto.transaction.TransactionRequest;
import com.financetracker.dto.transaction.TransactionResponse;
import com.financetracker.exception.ForbiddenOperationException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.TransactionMapper;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.model.User;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TransactionService.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private User otherUser;
    private Category category;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        otherUser = new User();
        otherUser.setId(2L);

        category = new Category();
        category.setId(1L);
        category.setName("Food");
        category.setSystemDefault(true);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setUser(user);
        transaction.setAmount(new BigDecimal("50.00"));
        transaction.setType(TransactionType.EXPENSE);
        transaction.setCategory(category);
        transaction.setDate(LocalDate.now());
        transaction.setCreatedAt(LocalDateTime.now());
    }

    private TransactionResponse buildResponse() {
        return new TransactionResponse(1L, new BigDecimal("50.00"), TransactionType.EXPENSE,
                1L, "Food", LocalDate.now(), null, LocalDateTime.now(), null);
    }

    @Test
    void createTransaction_successPath_withUserOwnedCategory() {
        var request = new TransactionRequest(new BigDecimal("50.00"), TransactionType.EXPENSE,
                1L, LocalDate.now(), null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any())).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(buildResponse());
        var result = transactionService.createTransaction(1L, request);
        assertThat(result).isNotNull();
    }

    @Test
    void createTransaction_successPath_withSystemDefaultCategory() {
        var request = new TransactionRequest(new BigDecimal("50.00"), TransactionType.INCOME,
                1L, LocalDate.now().plusDays(10), null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any())).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(buildResponse());
        var result = transactionService.createTransaction(1L, request);
        assertThat(result).isNotNull();
    }

    @Test
    void getTransaction_returnsCorrectResponseForOwnTransaction() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(buildResponse());
        var result = transactionService.getTransaction(1L, 1L);
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void getTransaction_throwsForbiddenOperationException_forAnotherUsersTransaction() {
        transaction.setUser(otherUser);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        assertThatThrownBy(() -> transactionService.getTransaction(1L, 1L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void updateTransaction_updatesAndReturnsUpdatedResponse() {
        var request = new TransactionRequest(new BigDecimal("75.00"), TransactionType.EXPENSE,
                1L, LocalDate.now(), "Updated");
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any())).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(buildResponse());
        var result = transactionService.updateTransaction(1L, 1L, request);
        assertThat(result).isNotNull();
    }

    @Test
    void deleteTransaction_callsRepositoryDelete() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        transactionService.deleteTransaction(1L, 1L);
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void getTransactions_returnsPaginatedResults() {
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findWithFilters(eq(1L), any(), any(), any(), any(),
                eq(pageable))).thenReturn(page);
        when(transactionMapper.toResponse(transaction)).thenReturn(buildResponse());
        var result = transactionService.getTransactions(1L,
                new TransactionFilterParams(null, null, null, null), pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void createTransaction_throwsResourceNotFoundException_whenCategoryNotFound() {
        var request = new TransactionRequest(new BigDecimal("50.00"), TransactionType.EXPENSE,
                99L, LocalDate.now(), null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> transactionService.createTransaction(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
