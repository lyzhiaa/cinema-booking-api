package kh.edu.istad.moviebooking.features.booking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BookingSeatResponse(
        UUID seatUuid,

        String seatLabel,

        BigDecimal unitPrice
) {
}
