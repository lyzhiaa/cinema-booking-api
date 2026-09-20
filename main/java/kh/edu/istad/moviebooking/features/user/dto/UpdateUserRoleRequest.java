package kh.edu.istad.moviebooking.features.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRoleRequest(

        @NotBlank(message = "Role is required")
        String role
) {
}
