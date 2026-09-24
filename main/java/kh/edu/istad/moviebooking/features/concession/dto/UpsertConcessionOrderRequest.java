package kh.edu.istad.moviebooking.features.concession.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpsertConcessionOrderRequest(

        @NotEmpty(message = "At least one concession item is required")
        List<@Valid ConcessionOrderItemRequest> items

) {
}