package kh.edu.istad.moviebooking.features.hall.dto;

import jakarta.validation.constraints.Positive;

public record HallUpdateRequest(
        @Positive(message = "Capacity must be greater than 0")
        Integer capacity
) {
}
