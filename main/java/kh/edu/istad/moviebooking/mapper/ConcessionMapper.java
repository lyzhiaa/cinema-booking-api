package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.ConcessionItem;
import kh.edu.istad.moviebooking.domain.ConcessionOrder;
import kh.edu.istad.moviebooking.domain.ConcessionOrderItem;
import kh.edu.istad.moviebooking.features.concession.dto.ConcessionItemResponse;
import kh.edu.istad.moviebooking.features.concession.dto.ConcessionOrderItemResponse;
import kh.edu.istad.moviebooking.features.concession.dto.ConcessionOrderResponse;
import kh.edu.istad.moviebooking.features.concession.dto.CreateConcessionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConcessionMapper {

    ConcessionItemResponse toConcessionItemResponse(ConcessionItem item);

    List<ConcessionItemResponse> toConcessionItemResponseList(List<ConcessionItem> items);

    @Mapping(source = "concessionItem.uuid", target = "concessionItemUuid")
    @Mapping(source = "itemName", target = "name")
    ConcessionOrderItemResponse toConcessionOrderItemResponse(ConcessionOrderItem item);

    List<ConcessionOrderItemResponse> toConcessionOrderItemResponseList(List<ConcessionOrderItem> items);

    @Mapping(source = "uuid", target = "orderUuid")
    @Mapping(source = "booking.uuid", target = "bookingUuid")
    @Mapping(source = "totalAmount", target = "concessionTotalAmount")
    @Mapping(source = "booking.totalAmount", target = "bookingTotalAmount")
    ConcessionOrderResponse toConcessionOrderResponse(ConcessionOrder order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ConcessionItem fromCreateRequest(CreateConcessionRequest request);
}