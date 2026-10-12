package com.financetracker.controller;

import com.financetracker.dto.transaction.TransactionFilterParams;
import com.financetracker.dto.transaction.TransactionRequest;
import com.financetracker.dto.transaction.TransactionResponse;
import com.financetracker.model.TransactionType;
import com.financetracker.model.User;
import com.financetracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for financial transaction management.
 * All endpoints require JWT authentication.
 */
@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Financial transaction management")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Constructs the TransactionController with the TransactionService dependency.
     *
     * @param transactionService the transaction management service
     */
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Returns a paginated, filtered list of transactions for the current user.
     *
     * @param type optional transaction type filter
     * @param categoryId optional category ID filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param pageable pagination parameters
     * @param authentication the current security authentication
     * @return 200 OK with paginated transactions
     */
    @GetMapping
    @Operation(summary = "Get paginated list of transactions with optional filters")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transactions retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable,
            Authentication authentication) {
        var userId = ((User) authentication.getPrincipal()).getId();
        var filters = new TransactionFilterParams(type, categoryId, startDate, endDate);
        return ResponseEntity.ok(transactionService.getTransactions(userId, filters, pageable));
    }

    /**
     * Creates a new transaction for the current user.
     *
     * @param request the transaction creation request
     * @param authentication the current security authentication
     * @return 201 Created with the new transaction
     */
    @PostMapping
    @Operation(summary = "Create a new transaction")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transaction created"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request, Authentication authentication) {
        var userId = ((User) authentication.getPrincipal()).getId();
        var created = transactionService.createTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Returns a single transaction by ID.
     *
     * @param id the transaction ID
     * @param authentication the current security authentication
     * @return 200 OK with the transaction
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a transaction by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long id,
                                                               Authentication authentication) {
        var userId = ((User) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(transactionService.getTransaction(userId, id));
    }

    /**
     * Updates an existing transaction by ID.
     *
     * @param id the transaction ID to update
     * @param request the update request
     * @param authentication the current security authentication
     * @return 200 OK with the updated transaction
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction updated"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {
        var userId = ((User) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(transactionService.updateTransaction(userId, id, request));
    }

    /**
     * Deletes a transaction by ID.
     *
     * @param id the transaction ID to delete
     * @param authentication the current security authentication
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Transaction deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id,
                                                   Authentication authentication) {
        var userId = ((User) authentication.getPrincipal()).getId();
        transactionService.deleteTransaction(userId, id);
        return ResponseEntity.noContent().build();
    }
}
