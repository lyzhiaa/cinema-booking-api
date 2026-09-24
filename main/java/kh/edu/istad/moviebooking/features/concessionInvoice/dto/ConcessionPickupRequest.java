package kh.edu.istad.moviebooking.features.concessionInvoice.dto;

import jakarta.validation.constraints.NotBlank;

public record ConcessionPickupRequest(

        @NotBlank
        String qrToken

) {
}