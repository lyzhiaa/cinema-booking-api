package kh.edu.istad.moviebooking.features.ticket.dto;

import kh.edu.istad.moviebooking.domain.enums.TicketStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TicketItemResponse(
        UUID ticketUuid,

        String movieTitle,

        String hallName,

        LocalDate showDate,

        LocalTime showTime,

        UUID seatUuid,

        String seatLabel,

        TicketStatus status


) {
}
