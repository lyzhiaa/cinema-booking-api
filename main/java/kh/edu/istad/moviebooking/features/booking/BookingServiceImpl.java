package kh.edu.istad.moviebooking.features.booking;

import jakarta.transaction.Transactional;
import kh.edu.istad.moviebooking.domain.*;
import kh.edu.istad.moviebooking.domain.enums.BookingStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.booking.dto.BookingResponse;
import kh.edu.istad.moviebooking.features.booking.dto.CreateBookingRequest;
import kh.edu.istad.moviebooking.features.seat.SeatRepository;
import kh.edu.istad.moviebooking.features.seatHold.SeatHoldService;
import kh.edu.istad.moviebooking.features.seatReservation.SeatReservationRepository;
import kh.edu.istad.moviebooking.features.showtime.ShowTimeRepository;
import kh.edu.istad.moviebooking.mapper.BookingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    //    Repository
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowTimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final SeatReservationRepository seatReservationRepository;

    //    mapper
    private final BookingMapper bookingMapper;
    //    Redis
    private final StringRedisTemplate stringRedisTemplate;
    //    service
    private final SeatHoldService seatHoldService;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest createBookingRequest) {

        // 1. Find Showtime
        Showtime showtime = showtimeRepository.findShowtimeByUuid(createBookingRequest.showtimeUuid())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "Showtime",
                                        "uuid",
                                        createBookingRequest.showtimeUuid()
                                )
                        );

        // 2. Prevent reusing same hold
        if (bookingRepository.existsBookingByHoldId(createBookingRequest.holdId())) {

            throw new BadRequestException("This seat hold has already been used"
            );
        }

        // 3. Find seats belonging to hold in Redis
        String holdSeatsKey = buildHoldSeatsKey(createBookingRequest.showtimeUuid(), createBookingRequest.holdId());

        Set<String> seatUuidStrings = stringRedisTemplate.opsForSet().members(holdSeatsKey);


        if (seatUuidStrings == null || seatUuidStrings.isEmpty()) {

            throw new BadRequestException("Seat hold does not exist or has expired");
        }

        List<Seat> seats = new ArrayList<>();

        // 4. Validate and load ALL seats
        for (String seatUuidString : seatUuidStrings) {

            UUID seatUuid = UUID.fromString(seatUuidString);

            String seatHoldKey = buildSeatHoldKey(createBookingRequest.showtimeUuid(), seatUuid);

            String currentHoldId = stringRedisTemplate.opsForValue().get(seatHoldKey);

            if (!createBookingRequest
                    .holdId()
                    .toString()
                    .equals(currentHoldId)) {

                throw new BadRequestException("Seat hold is invalid or has expired");
            }

            Seat seat = seatRepository.findSeatByUuid(seatUuid).orElseThrow(
                    () -> new ResourceNotFoundException("Seat", "uuid", seatUuid));

            if (!seat.getHall()
                    .getUuid()
                    .equals(showtime.getHall().getUuid())) {

                throw new BadRequestException("Seat does not belong to the showtime hall");
            }

            seats.add(seat);
        }

        // 5. Calculate price AFTER all seats are loaded
        BigDecimal unitPrice = showtime.getBasePrice();

        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(seats.size()));

        // 6. Create ONE Booking
        Booking booking =
                Booking.builder()
                        .showtime(showtime)
                        .holdId(createBookingRequest.holdId())
                        .status(BookingStatus.PENDING_PAYMENT)
                        .totalAmount(totalAmount)
                        .build();


        Booking savedBooking = bookingRepository.save(booking);

        // 7. Create BookingSeat for every seat
        List<BookingSeat> bookingSeats = new ArrayList<>();

        for (Seat bookedSeat : seats) {

            BookingSeat bookingSeat = BookingSeat
                            .builder()
                            .booking(savedBooking)
                            .seat(bookedSeat)
                            .unitPrice(unitPrice)
                            .build();

            bookingSeats.add(bookingSeat);
        }

        bookingSeatRepository.saveAll(bookingSeats);

        List<SeatReservation> reservations = new ArrayList<>();

        for (Seat seat : seats) {
            SeatReservation reservation = SeatReservation
                            .builder()
                            .showtime(showtime)
                            .seat(seat)
                            .booking(savedBooking)
                            .build();

            reservations.add(reservation);
        }

        try {
            seatReservationRepository.saveAllAndFlush(reservations);
        } catch (DataIntegrityViolationException exception) {

            throw new BadRequestException("One or more seats are already booked");
        }

        // 8. Remove temporary Redis hold
        seatHoldService.releaseHold(createBookingRequest.showtimeUuid(), createBookingRequest.holdId());

        // 9. Response
        return bookingMapper.toBookingResponse(savedBooking, bookingSeats);
    }

        @Override
        public BookingResponse getBookingByUuid (UUID bookingUuid){
            Booking booking = bookingRepository.findBookingByUuid(bookingUuid).orElseThrow(
                    () -> new ResourceNotFoundException(
                            "Booking",
                            "uuid",
                            bookingUuid
                    )
            );


            List<BookingSeat> bookingSeats = bookingSeatRepository.findAllBookingSeatByBookingUuid(bookingUuid);

            return bookingMapper.toBookingResponse(booking, bookingSeats);
        }

    private String buildHoldSeatsKey(UUID showtimeUuid, UUID holdId) {
        return "hold:seats:"
                + showtimeUuid
                + ":"
                + holdId;
    }
//    for verify hold ownership
    private String buildSeatHoldKey(UUID showtimeUuid, UUID seatUuid) {
        return "seat:hold:"
                + showtimeUuid
                + ":"
                + seatUuid;
    }

}
