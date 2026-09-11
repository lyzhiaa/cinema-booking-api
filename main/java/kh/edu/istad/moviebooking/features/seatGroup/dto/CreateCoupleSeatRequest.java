package kh.edu.istad.moviebooking.features.seatGroup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCoupleSeatRequest(
        @NotBlank(message = "Row label is required")
        String rowLabel,

        @NotNull(message = "First seat number is required")
        @Positive(message = "First seat number must be greater than 0")
        Integer firstSeatNumber
) {
}
