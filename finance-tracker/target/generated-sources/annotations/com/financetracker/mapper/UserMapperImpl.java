package com.financetracker.mapper;

import com.financetracker.dto.user.UserProfileResponse;
import com.financetracker.model.User;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-13T01:56:04+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserProfileResponse toProfileResponse(User user) {
        if ( user == null ) {
            return null;
        }

        String email = null;
        String displayName = null;
        LocalDateTime createdAt = null;
        Long id = null;

        email = user.getEmail();
        displayName = user.getDisplayName();
        createdAt = user.getCreatedAt();
        id = user.getId();

        UserProfileResponse userProfileResponse = new UserProfileResponse( id, email, displayName, createdAt );

        return userProfileResponse;
    }
}
