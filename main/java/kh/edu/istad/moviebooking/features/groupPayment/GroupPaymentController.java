package kh.edu.istad.moviebooking.features.groupPayment;

import kh.edu.istad.moviebooking.features.groupPayment.dto.GroupPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GroupPaymentController {

    private final GroupPaymentService groupPaymentService;

    @PostMapping("/api/v1/group-bookings/{groupUuid}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupPaymentResponse createPayment(@PathVariable UUID groupUuid) {

        return groupPaymentService
                .createPayment(groupUuid);
    }

    @GetMapping("/api/v1/group-payments/{paymentUuid}")
    public GroupPaymentResponse getPayment(@PathVariable UUID paymentUuid) {

        return groupPaymentService.getPayment(paymentUuid);
    }

    @GetMapping(
            value = "/api/v1/group-payments/{paymentUuid}/qr",
            produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> getQr(@PathVariable UUID paymentUuid) {

        byte[] qrImage = groupPaymentService.getQrImage(paymentUuid);

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }

    @PostMapping("/api/v1/group-payments/{paymentUuid}/verify")
    public GroupPaymentResponse verify(@PathVariable UUID paymentUuid) {

        return groupPaymentService.verifyPayment(paymentUuid);
    }
}