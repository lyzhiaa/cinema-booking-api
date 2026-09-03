package kh.edu.istad.moviebooking.features.hall.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateHallRequest(
        @NotBlank(message = "Hall name is required")
        String name,
        String description
) {
}
