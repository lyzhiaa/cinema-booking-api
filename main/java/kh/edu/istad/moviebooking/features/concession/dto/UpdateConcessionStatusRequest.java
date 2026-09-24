package kh.edu.istad.moviebooking.features.concession.dto;

import jakarta.validation.constraints.NotNull;
import kh.edu.istad.moviebooking.domain.enums.ConcessionOrderStatus;

public record UpdateConcessionStatusRequest(
        @NotNull
        ConcessionOrderStatus status
) {
}
