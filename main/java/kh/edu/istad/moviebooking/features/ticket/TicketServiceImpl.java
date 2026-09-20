package kh.edu.istad.moviebooking.features.ticket;

import kh.edu.istad.moviebooking.domain.*;
import kh.edu.istad.moviebooking.domain.enums.BookingStatus;
import kh.edu.istad.moviebooking.domain.enums.TicketStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.auth.CurrentUserService;
import kh.edu.istad.moviebooking.features.booking.BookingRepository;
import kh.edu.istad.moviebooking.features.booking.BookingSeatRepository;
import kh.edu.istad.moviebooking.features.common.PageResponse;
import kh.edu.istad.moviebooking.features.ticket.dto.DigitalTicketResponse;
import kh.edu.istad.moviebooking.features.ticket.dto.TicketItemResponse;
import kh.edu.istad.moviebooking.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService{
    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final CurrentUserService currentUserService;

    private final TicketMapper ticketMapper;

    @Override
    @Transactional
    public void generateTicketsForBooking(UUID bookingUuid) {

        Booking booking = bookingRepository.findBookingByUuid(bookingUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "uuid", bookingUuid));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Tickets can only be generated for a confirmed booking");
        }

        // Generate ONE QR token for entire booking
        if (booking.getTicketQrToken() == null) {

            booking.setTicketQrToken(UUID.randomUUID());
        }

        List<BookingSeat> bookingSeats = bookingSeatRepository.findAllBookingSeatByBookingUuid(bookingUuid);

        for (BookingSeat bookingSeat : bookingSeats) {

            boolean alreadyExists = ticketRepository.existsByBookingSeat_Id(bookingSeat.getId());

            if (!alreadyExists) {

                Ticket ticket = Ticket.builder()
                        .booking(booking)
                        .bookingSeat(bookingSeat)
                        .status(TicketStatus.ISSUED)
                        .build();

                ticketRepository.save(ticket);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DigitalTicketResponse getDigitalTicketsByQrToken(
            UUID qrToken
    ) {

        Booking booking = bookingRepository.findByTicketQrToken(qrToken)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Booking",
                                "ticketQrToken",
                                qrToken
                        )
                );

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Booking is not confirmed");
        }

        List<Ticket> tickets = ticketRepository.findAllByBookingUuid(booking.getUuid());

        List<TicketItemResponse> ticketItems = tickets.stream().map(ticket -> {

                            Booking ticketBooking = ticket.getBooking();

                            Showtime showtime = ticketBooking.getShowtime();

                            Seat seat = ticket.getBookingSeat().getSeat();

                            return new TicketItemResponse(

                                    ticket.getUuid(),

                                    showtime.getMovie()
                                            .getTitle(),

                                    showtime.getHall()
                                            .getName(),

                                    showtime.getStartTime()
                                            .toLocalDate(),

                                    showtime.getStartTime()
                                            .toLocalTime(),

                                    seat.getUuid(),

                                    seat.getSeatLabel(),

                                    ticket.getStatus()
                            );
                        })
                        .toList();

        return new DigitalTicketResponse(

                booking.getUuid(),

                booking.getTicketQrToken(),

                booking.getShowtime()
                        .getMovie()
                        .getTitle(),

                booking.getShowtime()
                        .getHall()
                        .getName(),

                booking.getShowtime()
                        .getStartTime(),

                booking.getStatus(),

                ticketItems
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TicketItemResponse> getMyTickets(int page, int size) {

        User user = currentUserService.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "issuedAt"));

        Page<Ticket> ticketPage = ticketRepository.findAllByBookingUserUuid(user.getUuid(), pageable);

        List<TicketItemResponse> tickets = ticketMapper.toTicketItemResponseList(ticketPage.getContent());

        return new PageResponse<>(
                tickets,
                ticketPage.getNumber(),
                ticketPage.getSize(),
                ticketPage.getTotalElements(),
                ticketPage.getTotalPages(),
                ticketPage.isFirst(),
                ticketPage.isLast()
        );
    }
}
