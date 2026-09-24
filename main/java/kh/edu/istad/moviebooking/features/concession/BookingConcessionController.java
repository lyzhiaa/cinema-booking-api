package kh.edu.istad.moviebooking.features.concession;

import jakarta.validation.Valid;
import kh.edu.istad.moviebooking.features.concession.dto.ConcessionOrderResponse;
import kh.edu.istad.moviebooking.features.concession.dto.UpsertConcessionOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings/{bookingUuid}/concession-order")
@RequiredArgsConstructor
public class BookingConcessionController {

    private final ConcessionOrderService concessionOrderService;

    @PutMapping
    public ConcessionOrderResponse
    addOrUpdateConcession(@PathVariable UUID bookingUuid, @Valid @RequestBody UpsertConcessionOrderRequest request) {

        return concessionOrderService.upsertConcessionOrder(bookingUuid, request);
    }

    @GetMapping
    public ConcessionOrderResponse
    getConcessionOrder(@PathVariable UUID bookingUuid) {

        return concessionOrderService.getPendingOrder(bookingUuid);
    }

    @DeleteMapping
    public ResponseEntity<Void>
    removeConcessionOrder(@PathVariable UUID bookingUuid) {

        concessionOrderService.removePendingOrder(bookingUuid);

        return ResponseEntity.noContent().build();
    }
}