package kh.edu.istad.moviebooking.features.payment;

import kh.edu.istad.moviebooking.features.payment.dto.PaymentResponse;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse createPayment(UUID bookingUuid);

//    testing if payment success or not
    PaymentResponse markPaymentSuccess(UUID paymentUuid);

    PaymentResponse getPaymentByUuid(UUID paymentUuid);

//    add failure when payment unsuccessfully
    PaymentResponse markPaymentFailed(UUID paymentUuid);
}
