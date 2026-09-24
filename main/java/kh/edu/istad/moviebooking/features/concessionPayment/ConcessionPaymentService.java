package kh.edu.istad.moviebooking.features.concessionPayment;

import kh.edu.istad.moviebooking.features.concessionPayment.dto.ConcessionPaymentResponse;

import java.util.UUID;

public interface ConcessionPaymentService {

    ConcessionPaymentResponse createPayment(UUID concessionOrderUuid);

    ConcessionPaymentResponse verifyPayment(UUID paymentUuid);
}
