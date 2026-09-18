package kh.edu.istad.moviebooking.features.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Size(min = 3, max = 100)
        String username,

        @Email(message = "Email is invalid")
        @Size(max = 150)
        String email,

        @Size(max = 30)
        String phone
) {
}
