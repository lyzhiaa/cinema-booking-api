package kh.edu.istad.moviebooking.features.booking.dto;

import kh.edu.istad.moviebooking.domain.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID uuid,

        UUID showtimeUuid,

        UUID userUuid,

        UUID ticketQrToken,

        String movieTitle,

        String hallName,

        LocalDateTime startTime,

        BookingStatus status,

        BigDecimal totalAmount,

        List<BookingSeatResponse> seats,

        LocalDateTime paymentExpiresAt,

        LocalDateTime createdAt

) {
}
