package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.Payment;
import kh.edu.istad.moviebooking.features.payment.dto.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(source = "booking.uuid", target = "bookingUuid")
    PaymentResponse toPaymentResponse(Payment payment);
}
