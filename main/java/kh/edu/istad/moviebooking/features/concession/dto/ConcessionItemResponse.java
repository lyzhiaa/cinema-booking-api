package kh.edu.istad.moviebooking.features.concession.dto;

import kh.edu.istad.moviebooking.domain.enums.ConcessionCategory;

import java.math.BigDecimal;
import java.util.UUID;

public record ConcessionItemResponse(

        UUID uuid,

        String name,

        String description,

        ConcessionCategory category,

        BigDecimal price,

        String imageUrl

) {
}