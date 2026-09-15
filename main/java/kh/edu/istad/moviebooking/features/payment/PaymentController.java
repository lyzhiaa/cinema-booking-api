package kh.edu.istad.moviebooking.features.payment;


import kh.edu.istad.moviebooking.features.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/bookings/{bookingUuid}/payments")
    public PaymentResponse  createPayment(@PathVariable UUID bookingUuid) {
        return paymentService.createPayment(bookingUuid);
    }

//    @PatchMapping("/payments/{paymentUuid}/success")
//    public PaymentResponse markPaymentSuccess(@PathVariable UUID paymentUuid) {
//        return paymentService.markPaymentSuccess(paymentUuid);
//    }

    @GetMapping("/payments/{paymentUuid}")
    public PaymentResponse getPayment(@PathVariable UUID paymentUuid) {
        return paymentService.getPaymentByUuid(paymentUuid);
    }

    @PatchMapping("/payments/{paymentUuid}/failed")
    public PaymentResponse markPaymentFailed(@PathVariable UUID paymentUuid) {
        return paymentService.markPaymentFailed(paymentUuid);
    }
}
