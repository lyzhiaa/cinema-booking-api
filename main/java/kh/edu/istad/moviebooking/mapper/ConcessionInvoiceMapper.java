package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.ConcessionInvoice;
import kh.edu.istad.moviebooking.features.concessionInvoice.dto.ConcessionInvoiceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ConcessionOrderItemMapper.class)
public interface ConcessionInvoiceMapper {

    @Mapping(target = "concessionOrderUuid", source = "concessionOrder.uuid")
    @Mapping(target = "bookingUuid", source = "concessionOrder.booking.uuid")
    @Mapping(target = "totalAmount", source = "concessionOrder.totalAmount")
    ConcessionInvoiceResponse toResponse(ConcessionInvoice invoice);
}
