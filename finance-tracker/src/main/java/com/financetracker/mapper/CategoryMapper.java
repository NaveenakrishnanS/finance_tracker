package com.financetracker.mapper;

import com.financetracker.dto.category.CategoryResponse;
import com.financetracker.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between {@link Category} entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /**
     * Maps a Category entity to a CategoryResponse DTO.
     *
     * @param category the Category entity to map
     * @return the CategoryResponse DTO
     */
    @Mapping(source = "systemDefault", target = "isSystemDefault")
    CategoryResponse toResponse(Category category);
}
