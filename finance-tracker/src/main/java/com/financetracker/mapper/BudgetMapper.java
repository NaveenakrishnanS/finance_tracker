package com.financetracker.mapper;

import com.financetracker.dto.budget.BudgetResponse;
import com.financetracker.model.Budget;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between {@link Budget} entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface BudgetMapper {

    /**
     * Maps a Budget entity plus a computed spentAmount to a BudgetResponse DTO.
     * The overLimit field is computed as spentAmount greater than limitAmount.
     *
     * @param budget the Budget entity to map
     * @param spentAmount the computed amount spent in this budget period
     * @return the BudgetResponse DTO
     */
    @Mapping(source = "budget.id", target = "id")
    @Mapping(source = "budget.category.id", target = "categoryId")
    @Mapping(source = "budget.category.name", target = "categoryName")
    @Mapping(source = "budget.limitAmount", target = "limitAmount")
    @Mapping(source = "budget.month", target = "month")
    @Mapping(source = "budget.year", target = "year")
    @Mapping(source = "spentAmount", target = "spentAmount")
    @Mapping(expression = "java(spentAmount.compareTo(budget.getLimitAmount()) > 0)", target = "overLimit")
    BudgetResponse toResponse(Budget budget, BigDecimal spentAmount);
}
