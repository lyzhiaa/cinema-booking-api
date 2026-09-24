package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.ConcessionOrder;
import kh.edu.istad.moviebooking.features.concession.dto.ConcessionOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConcessionOrderMapper {

    @Mapping(target = "bookingUuid", source = "booking.uuid")
    ConcessionOrderResponse toResponse(ConcessionOrder concessionOrder);
}