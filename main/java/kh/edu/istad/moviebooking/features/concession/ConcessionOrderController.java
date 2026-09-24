package kh.edu.istad.moviebooking.features.concession;

import jakarta.validation.Valid;
import kh.edu.istad.moviebooking.features.concession.dto.ConcessionOrderResponse;
import kh.edu.istad.moviebooking.features.concession.dto.CreatePostBookingConcessionRequest;
import kh.edu.istad.moviebooking.features.concession.dto.EligibleConcessionBookingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/concession-orders")
public class ConcessionOrderController {

    private final ConcessionOrderService concessionOrderService;

    @GetMapping("/eligible-bookings")
    public List<EligibleConcessionBookingResponse> getEligibleBookings() {
        return concessionOrderService.getEligibleBookings();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConcessionOrderResponse createPostBookingOrder(
            @Valid
            @RequestBody
            CreatePostBookingConcessionRequest createPostBookingConcessionRequest
    ) {
        return concessionOrderService.createPostBookingOrder(createPostBookingConcessionRequest);
    }
}