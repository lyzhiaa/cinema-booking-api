package kh.edu.istad.moviebooking.features.concession.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ConcessionOrderItemResponse(

        UUID uuid,

        UUID concessionItemUuid,

        String name,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal subtotal

) {
}