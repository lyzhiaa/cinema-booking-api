package kh.edu.istad.moviebooking.features.payment.dto;

import kh.edu.istad.moviebooking.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreatePaymentResponse(
        UUID paymentUuid,

        UUID bookingUuid,

        BigDecimal amount,

        PaymentStatus status,

        String qrPayload,

        LocalDateTime paymentExpiresAt
) {
}
