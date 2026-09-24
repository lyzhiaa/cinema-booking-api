package kh.edu.istad.moviebooking.features.concession.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import kh.edu.istad.moviebooking.domain.enums.ConcessionCategory;

import java.math.BigDecimal;

public record UpdateConcessionRequest(

        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
        String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        ConcessionCategory category,

        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        @Size(max = 500, message = "Image URL cannot exceed 500 characters")
        String imageUrl

) {
}
