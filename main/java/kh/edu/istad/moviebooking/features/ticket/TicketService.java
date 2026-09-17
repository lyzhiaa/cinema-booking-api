package kh.edu.istad.moviebooking.features.ticket;


import kh.edu.istad.moviebooking.features.ticket.dto.DigitalTicketResponse;

import java.util.UUID;

public interface TicketService {

//    sed after payment success
    void generateTicketsForBooking(UUID bookingUuid);

//    used when customer scans QR
    DigitalTicketResponse getDigitalTicketsByQrToken(UUID qrToken);
}
