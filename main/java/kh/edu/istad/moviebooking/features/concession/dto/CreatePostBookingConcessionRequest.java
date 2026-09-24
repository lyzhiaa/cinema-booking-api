package kh.edu.istad.moviebooking.features.concession.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreatePostBookingConcessionRequest(

        @NotNull
        UUID bookingUuid,

        @NotEmpty
        List<ConcessionOrderItemRequest> items

) {
}
