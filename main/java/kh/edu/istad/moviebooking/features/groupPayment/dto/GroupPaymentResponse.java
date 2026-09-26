package kh.edu.istad.moviebooking.features.groupPayment.dto;

import kh.edu.istad.moviebooking.domain.enums.PaymentMethod;
import kh.edu.istad.moviebooking.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record GroupPaymentResponse(

        UUID uuid,

        UUID groupUuid,

        BigDecimal amount,

        PaymentStatus status,

        PaymentMethod paymentMethod,

        LocalDateTime paidAt

) {
}