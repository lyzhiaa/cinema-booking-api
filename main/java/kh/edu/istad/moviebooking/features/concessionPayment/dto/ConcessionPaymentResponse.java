package kh.edu.istad.moviebooking.features.concessionPayment.dto;

import kh.edu.istad.moviebooking.domain.enums.PaymentMethod;
import kh.edu.istad.moviebooking.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ConcessionPaymentResponse(
        UUID paymentUuid,
        UUID concessionOrderUuid,
        BigDecimal amount,
        PaymentStatus status,
        PaymentMethod method,
        String qrPayload,
        LocalDateTime expiresAt
) {
}
