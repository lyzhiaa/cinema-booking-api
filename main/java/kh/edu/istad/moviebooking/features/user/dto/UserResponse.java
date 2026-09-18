package kh.edu.istad.moviebooking.features.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID uuid,

        String firstName,

        String lastName,

        String username,

        String email,

        String phone,

        Integer points,

        Boolean disabled,

        Boolean isDeleted,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
