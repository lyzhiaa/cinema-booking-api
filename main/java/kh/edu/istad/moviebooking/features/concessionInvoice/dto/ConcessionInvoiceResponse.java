package kh.edu.istad.moviebooking.features.concessionInvoice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ConcessionInvoiceResponse(

        UUID uuid,

        String invoiceNumber,

        UUID concessionOrderUuid,

        UUID bookingUuid,

        BigDecimal totalAmount,

        String qrToken,

        LocalDateTime issuedAt,

        LocalDateTime pickedUpAt

) {
}