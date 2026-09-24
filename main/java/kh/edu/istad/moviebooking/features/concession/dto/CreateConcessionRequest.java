package kh.edu.istad.moviebooking.features.concession.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kh.edu.istad.moviebooking.domain.enums.ConcessionCategory;

import java.math.BigDecimal;

public record CreateConcessionRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,

        String description,

        @NotNull(message = "Category is required")
        ConcessionCategory category,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        String imageUrl
) {
}
