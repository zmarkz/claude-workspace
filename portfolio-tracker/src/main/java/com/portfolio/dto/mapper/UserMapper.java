package com.portfolio.dto.mapper;

import com.portfolio.dto.response.UserResponse;
import com.portfolio.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }
}
