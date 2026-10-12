package com.financetracker.mapper;

import com.financetracker.dto.category.CategoryResponse;
import com.financetracker.model.Category;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-13T01:56:04+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public CategoryResponse toResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        boolean isSystemDefault = false;
        Long id = null;
        String name = null;

        isSystemDefault = category.isSystemDefault();
        id = category.getId();
        name = category.getName();

        CategoryResponse categoryResponse = new CategoryResponse( id, name, isSystemDefault );

        return categoryResponse;
    }
}
