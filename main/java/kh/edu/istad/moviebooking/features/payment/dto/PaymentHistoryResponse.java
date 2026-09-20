package kh.edu.istad.moviebooking.features.payment.dto;

import kh.edu.istad.moviebooking.domain.enums.BookingStatus;
import kh.edu.istad.moviebooking.domain.enums.PaymentMethod;
import kh.edu.istad.moviebooking.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentHistoryResponse(
        UUID paymentUuid,

        BigDecimal amount,

        PaymentStatus paymentStatus,

        PaymentMethod paymentMethod,

        String transactionReference,

        LocalDateTime paidAt,

        LocalDateTime createdAt,

        UUID bookingUuid,

        BookingStatus bookingStatus,

        UUID movieUuid,

        String movieTitle,

        String posterPath,

        LocalDateTime showtime,

        String hallName

) {
}
