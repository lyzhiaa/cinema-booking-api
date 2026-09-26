package kh.edu.istad.moviebooking.features.groupPayment;

import kh.edu.istad.moviebooking.features.groupPayment.dto.GroupPaymentResponse;

import java.util.UUID;

public interface GroupPaymentService {

    GroupPaymentResponse createPayment(UUID groupUuid);

    GroupPaymentResponse getPayment(UUID paymentUuid);

    byte[] getQrImage(UUID paymentUuid);

    GroupPaymentResponse verifyPayment(UUID paymentUuid);
}