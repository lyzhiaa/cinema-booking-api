package kh.edu.istad.moviebooking.features.ticket;


import kh.edu.istad.moviebooking.features.common.PageResponse;
import kh.edu.istad.moviebooking.features.ticket.dto.DigitalTicketResponse;
import kh.edu.istad.moviebooking.features.ticket.dto.TicketItemResponse;

import java.util.UUID;

public interface TicketService {

//    sed after payment success
    void generateTicketsForBooking(UUID bookingUuid);

//    used when customer scans QR
    DigitalTicketResponse getDigitalTicketsByQrToken(UUID qrToken);

    PageResponse<TicketItemResponse> getMyTickets(int page, int size);
}
