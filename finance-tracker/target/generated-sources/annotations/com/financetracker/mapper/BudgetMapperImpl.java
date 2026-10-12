package com.financetracker.mapper;

import com.financetracker.dto.budget.BudgetResponse;
import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-13T01:56:04+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class BudgetMapperImpl implements BudgetMapper {

    @Override
    public BudgetResponse toResponse(Budget budget, BigDecimal spentAmount) {
        if ( budget == null && spentAmount == null ) {
            return null;
        }

        Long id = null;
        Long categoryId = null;
        String categoryName = null;
        BigDecimal limitAmount = null;
        int month = 0;
        int year = 0;
        if ( budget != null ) {
            id = budget.getId();
            categoryId = budgetCategoryId( budget );
            categoryName = budgetCategoryName( budget );
            limitAmount = budget.getLimitAmount();
            month = budget.getMonth();
            year = budget.getYear();
        }
        BigDecimal spentAmount1 = null;
        spentAmount1 = spentAmount;

        boolean overLimit = spentAmount.compareTo(budget.getLimitAmount()) > 0;

        BudgetResponse budgetResponse = new BudgetResponse( id, categoryId, categoryName, limitAmount, month, year, spentAmount1, overLimit );

        return budgetResponse;
    }

    private Long budgetCategoryId(Budget budget) {
        if ( budget == null ) {
            return null;
        }
        Category category = budget.getCategory();
        if ( category == null ) {
            return null;
        }
        Long id = category.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String budgetCategoryName(Budget budget) {
        if ( budget == null ) {
            return null;
        }
        Category category = budget.getCategory();
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
