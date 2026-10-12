package com.financetracker.mapper;

import com.financetracker.dto.transaction.TransactionResponse;
import com.financetracker.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting {@link Transaction} entities to DTOs.
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * Maps a Transaction entity to a TransactionResponse DTO.
     *
     * @param transaction the Transaction entity to map
     * @return the TransactionResponse DTO
     */
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    TransactionResponse toResponse(Transaction transaction);
}
