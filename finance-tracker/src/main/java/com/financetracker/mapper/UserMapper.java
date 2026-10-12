package com.financetracker.mapper;

import com.financetracker.dto.user.UserProfileResponse;
import com.financetracker.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between {@link User} entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Maps a User entity to a UserProfileResponse DTO.
     *
     * @param user the User entity to map
     * @return the UserProfileResponse DTO
     */
    @Mapping(source = "email", target = "email")
    @Mapping(source = "displayName", target = "displayName")
    @Mapping(source = "createdAt", target = "createdAt")
    UserProfileResponse toProfileResponse(User user);
}
