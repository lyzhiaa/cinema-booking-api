package kh.edu.istad.moviebooking.features.concession.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EligibleConcessionBookingResponse(
        UUID bookingUuid,
        UUID showtimeUuid,
        String movieTitle,
        String hallName,
        LocalDateTime startTime,
        List<String> seats
) {
}
