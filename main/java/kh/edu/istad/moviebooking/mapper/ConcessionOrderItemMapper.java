package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.ConcessionOrderItem;
import kh.edu.istad.moviebooking.features.concession.dto.ConcessionOrderItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConcessionOrderItemMapper {

    @Mapping(target = "concessionItemUuid", source = "concessionItem.uuid")
    ConcessionOrderItemResponse toResponse(ConcessionOrderItem item);
}