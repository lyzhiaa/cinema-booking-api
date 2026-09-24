package kh.edu.istad.moviebooking.features.concession.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConcessionOrderItemRequest(

        @NotNull(message = "Concession item UUID is required")
        UUID concessionItemUuid,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 20, message = "Quantity cannot exceed 20")
        Integer quantity

) {
}