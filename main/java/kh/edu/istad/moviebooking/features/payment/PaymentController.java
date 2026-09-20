package kh.edu.istad.moviebooking.features.payment;


import kh.edu.istad.moviebooking.domain.Payment;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.common.PageResponse;
import kh.edu.istad.moviebooking.features.payment.dto.PaymentHistoryResponse;
import kh.edu.istad.moviebooking.features.payment.dto.PaymentResponse;
import kh.edu.istad.moviebooking.features.qrcode.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final QrCodeService qrCodeService;

    @PostMapping("/bookings/{bookingUuid}/payments")
    public PaymentResponse  createPayment(@PathVariable UUID bookingUuid) {
        return paymentService.createPayment(bookingUuid);
    }

    @PatchMapping("/payments/{paymentUuid}/success")
    public PaymentResponse markPaymentSuccess(@PathVariable UUID paymentUuid) {
        return paymentService.markPaymentSuccess(paymentUuid);
    }

    @GetMapping("/payments/{paymentUuid}")
    public PaymentResponse getPayment(@PathVariable UUID paymentUuid) {
        return paymentService.getPaymentByUuid(paymentUuid);
    }

    @PatchMapping("/payments/{paymentUuid}/failed")
    public PaymentResponse markPaymentFailed(@PathVariable UUID paymentUuid) {
        return paymentService.markPaymentFailed(paymentUuid);
    }

    @GetMapping(value = "/payments/{paymentUuid}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getPaymentQr(@PathVariable UUID paymentUuid) {

        Payment payment = paymentRepository.findPaymentByUuid(paymentUuid)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "Payment",
                                        "uuid",
                                        paymentUuid
                                )
                        );

        byte[] qr = qrCodeService.generateQrCode(payment.getQrPayload(),
                        400,
                        400
                );

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qr);

    }

    @PostMapping("/payments/{paymentUuid}/verify")
    public PaymentResponse verifyPayment(@PathVariable UUID paymentUuid) {

        return paymentService.verifyBakongPayment(paymentUuid);
    }

//    get my payment
    @GetMapping("/payments/me")
    public PageResponse<PaymentHistoryResponse> getMyPayments(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size

    ) {
        return paymentService.getMyPayments(page, size);
    }
}
