package kh.edu.istad.moviebooking.features.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingExpirationScheduler {

    private final BookingServiceImpl bookingService;

    @Scheduled(fixedRate = 60000)
    public void expireBookings() {
        bookingService.expirePendingBookings();;
    }
}
