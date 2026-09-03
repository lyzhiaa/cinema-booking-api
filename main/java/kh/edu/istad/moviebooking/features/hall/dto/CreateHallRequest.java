package kh.edu.istad.moviebooking.features.hall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kh.edu.istad.moviebooking.domain.enums.HallType;

public record CreateHallRequest(
        @NotBlank(message = "Hall name is required")
        String name,
        String description,
//        update
        @Positive(message = "Capacity must be greater than 0")
        Integer capacity,
        @NotNull
        HallType hallType
) {
}
