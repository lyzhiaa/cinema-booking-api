package kh.edu.istad.moviebooking.features.booking;

import kh.edu.istad.moviebooking.features.booking.dto.BookingResponse;
import kh.edu.istad.moviebooking.features.booking.dto.CreateBookingRequest;
import kh.edu.istad.moviebooking.features.common.PageResponse;

import java.util.UUID;

public interface BookingService {
//    create booking
    BookingResponse createBooking(CreateBookingRequest createBookingRequest);

//    get booking by uuid
    BookingResponse getBookingByUuid(UUID bookingUuid);

//    get booking by userUuid
    PageResponse<BookingResponse> getMyBookings(int page, int size);

    void expirePendingBookings();
}
