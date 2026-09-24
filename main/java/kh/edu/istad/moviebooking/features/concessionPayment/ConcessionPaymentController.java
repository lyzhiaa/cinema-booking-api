package kh.edu.istad.moviebooking.features.concessionPayment;

import kh.edu.istad.moviebooking.features.concessionPayment.dto.ConcessionPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/concession-payments")
@RequiredArgsConstructor
public class ConcessionPaymentController {

    private final ConcessionPaymentService concessionPaymentService;

    @PostMapping("/orders/{concessionOrderUuid}")
    public ConcessionPaymentResponse createPayment(@PathVariable UUID concessionOrderUuid) {
        return concessionPaymentService.createPayment(concessionOrderUuid);
    }

    @PostMapping("/{paymentUuid}/verify")
    public ConcessionPaymentResponse verifyPayment(@PathVariable UUID paymentUuid) {
        return concessionPaymentService.verifyPayment(paymentUuid);
    }
}
