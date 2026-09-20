package kh.edu.istad.moviebooking.features.booking;

import kh.edu.istad.moviebooking.domain.*;
import kh.edu.istad.moviebooking.domain.enums.BookingStatus;
import kh.edu.istad.moviebooking.domain.enums.SeatAvailabilityStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.auth.CurrentUserService;
import kh.edu.istad.moviebooking.features.booking.dto.BookingResponse;
import kh.edu.istad.moviebooking.features.booking.dto.CreateBookingRequest;
import kh.edu.istad.moviebooking.features.seat.SeatRealtimeService;
import kh.edu.istad.moviebooking.features.seat.SeatRepository;
import kh.edu.istad.moviebooking.features.seatHold.SeatHoldService;
import kh.edu.istad.moviebooking.features.seatReservation.SeatReservationRepository;
import kh.edu.istad.moviebooking.features.showtime.ShowTimeRepository;
import kh.edu.istad.moviebooking.features.user.UserRepository;
import kh.edu.istad.moviebooking.mapper.BookingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final UserRepository userRepository;

    //    mapper
    private final BookingMapper bookingMapper;
    //    Redis
    private final StringRedisTemplate stringRedisTemplate;
    //    service
    private final SeatHoldService seatHoldService;

    private final SeatRealtimeService seatRealtimeService;

    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest createBookingRequest) {

        // find user
        User user = currentUserService.getCurrentUser();

        // 1. Find Showtime
        Showtime showtime = showtimeRepository.findShowtimeByUuid(createBookingRequest.showtimeUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Showtime",
                                "uuid",
                                createBookingRequest.showtimeUuid()
                        )
                );

        // reject disable account
        if (Boolean.TRUE.equals(user.getDisabled())) {
            throw new BadRequestException("User account is disabled");
        }

        seatHoldService.validateHoldOwner(
                createBookingRequest.showtimeUuid(),
                createBookingRequest.holdId(),
                user.getUuid()
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
        Booking booking = Booking.builder()
                        .user(user)
                        .showtime(showtime)
                        .holdId(createBookingRequest.holdId())
                        .status(BookingStatus.PENDING_PAYMENT)
                        .totalAmount(totalAmount)
                        .paymentExpiresAt(LocalDateTime.now().plusMinutes(10))
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

        List<UUID> bookedSeatUuids = seats.stream()
                .map(Seat::getUuid)
                .toList();

        seatRealtimeService.broadcastSeatUpdate(
                showtime.getUuid(),
                bookedSeatUuids,
                SeatAvailabilityStatus.BOOKED,
                null
        );

        // 8. Remove temporary Redis hold
        seatHoldService.releaseHold(createBookingRequest.showtimeUuid(), createBookingRequest.holdId());

        // 9. Response
        return bookingMapper.toBookingResponse(savedBooking, bookingSeats);
    }

//    get booking by uuid
        @Override
        @Transactional
        public BookingResponse getBookingByUuid (UUID bookingUuid){
//        finding the booking
            Booking booking = bookingRepository.findBookingByUuid(bookingUuid).orElseThrow
                    (() -> new ResourceNotFoundException("Booking", "uuid", bookingUuid)
            );

            //  Check whether unpaid booking has expired
            expireBookingIfNeeded(booking);

            // get booking seats
            List<BookingSeat> bookingSeats = bookingSeatRepository.findAllBookingSeatByBookingUuid(bookingUuid);

            return bookingMapper.toBookingResponse(booking, bookingSeats);
        }


    //        get booking by user uuid
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBooking() {

        User user = currentUserService.getCurrentUser();

        List<Booking> bookings = bookingRepository.findAllByUserUuidOrderByCreatedAtDesc(user.getUuid());

        return bookings.stream().map(booking -> {

                    List<BookingSeat> bookingSeats = bookingSeatRepository
                                    .findAllBookingSeatByBookingUuid(booking.getUuid());

                    return bookingMapper.toBookingResponse(booking, bookingSeats);
                }).toList();
    }

    //        expiration method
    @Transactional
    public void expirePendingBookings() {
        List<Booking> expiredBookings = bookingRepository.findAllByStatusAndPaymentExpiresAtBefore(
                BookingStatus.PENDING_PAYMENT,
                LocalDateTime.now()
        );

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.EXPIRED);

            seatReservationRepository.deleteAllByBookingUuid(booking.getUuid());
        }
    }

    private String buildHoldSeatsKey(UUID showtimeUuid, UUID holdId) {
        return "hold:seats:" + showtimeUuid + ":" + holdId;
    }
//    for verify hold ownership
    private String buildSeatHoldKey(UUID showtimeUuid, UUID seatUuid) {
        return "seat:hold:" + showtimeUuid + ":" + seatUuid;
    }

//    check when booking is success
    private void expireBookingIfNeeded(Booking booking) {
        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT && booking.getPaymentExpiresAt().isBefore(LocalDateTime.now())) {
            booking.setStatus(BookingStatus.EXPIRED);

            seatReservationRepository.deleteAllByBookingUuid(booking.getUuid());
        }
    }

}
