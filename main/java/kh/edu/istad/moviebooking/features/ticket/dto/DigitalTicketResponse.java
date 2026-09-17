package kh.edu.istad.moviebooking.features.ticket.dto;

import kh.edu.istad.moviebooking.domain.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DigitalTicketResponse(
        UUID bookingUuid,

        UUID ticketQrToken,

        String movieTitle,

        String hallName,

        LocalDateTime startTime,

        BookingStatus bookingStatus,

        List<TicketItemResponse> tickets
) {
}
