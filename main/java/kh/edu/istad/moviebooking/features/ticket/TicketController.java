package kh.edu.istad.moviebooking.features.ticket;

import kh.edu.istad.moviebooking.domain.Booking;
import kh.edu.istad.moviebooking.domain.enums.BookingStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.booking.BookingRepository;
import kh.edu.istad.moviebooking.features.common.PageResponse;
import kh.edu.istad.moviebooking.features.qrcode.QrCodeService;
import kh.edu.istad.moviebooking.features.ticket.dto.DigitalTicketResponse;
import kh.edu.istad.moviebooking.features.ticket.dto.TicketItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    private final QrCodeService qrCodeService;
    private final BookingRepository bookingRepository;


    @GetMapping("/qr/{qrToken}")
    public DigitalTicketResponse getDigitalTicket(@PathVariable UUID qrToken) {

        return ticketService.getDigitalTicketsByQrToken(qrToken);
    }

    @GetMapping(value = "/bookings/{bookingUuid}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getBookingQrCode(@PathVariable UUID bookingUuid) {

        Booking booking = bookingRepository.findBookingByUuid(bookingUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Booking",
                                "uuid",
                                bookingUuid
                        )
                );

        if (
                booking.getStatus() != BookingStatus.CONFIRMED
        ) {
            throw new BadRequestException("QR code is only available for confirmed bookings");
        }

        if (booking.getTicketQrToken() == null) {
            throw new BadRequestException("Ticket QR token has not been generated");
        }

        String ticketUrl = "http://localhost:3000/tickets/" + booking.getTicketQrToken();

        byte[] qrCode = qrCodeService.generateQrCode(ticketUrl, 400, 400);

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrCode);
    }

    @GetMapping("/me")
    public PageResponse<TicketItemResponse> getMyTickets(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size

    ) {

        return ticketService.getMyTickets(page, size);
    }
}
