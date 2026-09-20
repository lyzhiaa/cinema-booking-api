package kh.edu.istad.moviebooking.features.booking;

import jakarta.validation.Valid;
import kh.edu.istad.moviebooking.features.booking.dto.BookingResponse;
import kh.edu.istad.moviebooking.features.booking.dto.CreateBookingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;


    @PostMapping
    public BookingResponse createBooking(@Valid @RequestBody CreateBookingRequest createBookingRequest) {

        return bookingService.createBooking(createBookingRequest);
    }


    @GetMapping("/{uuid}")
    public BookingResponse getBookingByUuid(@PathVariable UUID uuid) {

        return bookingService.getBookingByUuid(uuid);
    }

    @GetMapping("/users/me")
    public List<BookingResponse> getMyBooking() {

        return bookingService.getMyBooking();
    }
}
