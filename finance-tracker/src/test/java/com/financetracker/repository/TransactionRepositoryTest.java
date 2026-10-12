package com.financetracker.repository;

import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DataJpaTest for TransactionRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("txn-test@example.com");
        user.setPasswordHash("hash");
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        category = new Category();
        category.setName("TestCategory");
        category.setSystemDefault(true);
        category = categoryRepository.save(category);
    }

    private Transaction buildTransaction(BigDecimal amount, TransactionType type, LocalDate date) {
        var t = new Transaction();
        t.setUser(user);
        t.setAmount(amount);
        t.setType(type);
        t.setCategory(category);
        t.setDate(date);
        t.setCreatedAt(LocalDateTime.now());
        return t;
    }

    @Test
    void findByUserIdOrderByDateDesc_returnsOnlyUserTransactions() {
        var other = new User();
        other.setEmail("other@example.com");
        other.setPasswordHash("hash");
        other.setCreatedAt(LocalDateTime.now());
        other = userRepository.save(other);

        transactionRepository.save(buildTransaction(
                new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.now()));
        var otherTxn = new Transaction();
        otherTxn.setUser(other);
        otherTxn.setAmount(new BigDecimal("50.00"));
        otherTxn.setType(TransactionType.EXPENSE);
        otherTxn.setCategory(category);
        otherTxn.setDate(LocalDate.now());
        otherTxn.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(otherTxn);

        var page = transactionRepository.findByUserIdOrderByDateDesc(
                user.getId(), PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findWithFilters_filterByType_returnsOnlyMatchingType() {
        transactionRepository.save(buildTransaction(
                new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.now()));
        transactionRepository.save(buildTransaction(
                new BigDecimal("50.00"), TransactionType.EXPENSE, LocalDate.now()));

        var page = transactionRepository.findWithFilters(user.getId(),
                TransactionType.INCOME, null, null, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).allMatch(t -> t.getType() == TransactionType.INCOME);
    }

    @Test
    void findWithFilters_filterByDateRange_returnsOnlyInRange() {
        var past = LocalDate.now().minusDays(30);
        var recent = LocalDate.now();
        transactionRepository.save(buildTransaction(new BigDecimal("100.00"),
                TransactionType.INCOME, past));
        transactionRepository.save(buildTransaction(new BigDecimal("50.00"),
                TransactionType.INCOME, recent));

        var page = transactionRepository.findWithFilters(user.getId(), null, null,
                LocalDate.now().minusDays(1), LocalDate.now(), PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getDate()).isEqualTo(recent);
    }

    @Test
    void bigDecimalRoundTrip_preservesFourDecimalPlaces() {
        var amount = new BigDecimal("1234.5678");
        var txn = buildTransaction(amount, TransactionType.INCOME, LocalDate.now());
        var saved = transactionRepository.saveAndFlush(txn);
        var retrieved = transactionRepository.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.getAmount().compareTo(amount)).isZero();
    }
}
