package kh.edu.istad.moviebooking.mapper;


import kh.edu.istad.moviebooking.domain.ConcessionPayment;
import kh.edu.istad.moviebooking.features.concessionPayment.dto.ConcessionPaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConcessionPaymentMapper {

    @Mapping(target = "paymentUuid", source = "uuid")
    @Mapping(target = "concessionOrderUuid", source = "concessionOrder.uuid")
    kh.edu.istad.moviebooking.features.concessionPayment.dto.ConcessionPaymentResponse toResponse(ConcessionPayment concessionPayment);
}
