package kh.edu.istad.moviebooking.features.concession.dto;

import kh.edu.istad.moviebooking.domain.enums.ConcessionOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ConcessionOrderResponse(

        UUID orderUuid,

        UUID bookingUuid,

        ConcessionOrderStatus status,

        BigDecimal concessionTotalAmount,

        BigDecimal bookingTotalAmount,

        List<ConcessionOrderItemResponse> items,

        LocalDateTime createdAt

) {
}