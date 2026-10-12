package com.financetracker.mapper;

import com.financetracker.dto.transaction.TransactionResponse;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-13T01:56:04+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public TransactionResponse toResponse(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        Long categoryId = null;
        String categoryName = null;
        Long id = null;
        BigDecimal amount = null;
        TransactionType type = null;
        LocalDate date = null;
        String description = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        categoryId = transactionCategoryId( transaction );
        categoryName = transactionCategoryName( transaction );
        id = transaction.getId();
        amount = transaction.getAmount();
        type = transaction.getType();
        date = transaction.getDate();
        description = transaction.getDescription();
        createdAt = transaction.getCreatedAt();
        updatedAt = transaction.getUpdatedAt();

        TransactionResponse transactionResponse = new TransactionResponse( id, amount, type, categoryId, categoryName, date, description, createdAt, updatedAt );

        return transactionResponse;
    }

    private Long transactionCategoryId(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }
        Category category = transaction.getCategory();
        if ( category == null ) {
            return null;
        }
        Long id = category.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String transactionCategoryName(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }
        Category category = transaction.getCategory();
        if ( category == null ) {
            return null;
        }
        String name = category.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
