package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.Payment;
import kh.edu.istad.moviebooking.features.payment.dto.PaymentHistoryResponse;
import kh.edu.istad.moviebooking.features.payment.dto.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(source = "booking.uuid", target = "bookingUuid")
    PaymentResponse toPaymentResponse(Payment payment);

    @Mapping(source = "uuid", target = "paymentUuid")
    @Mapping(source = "status", target = "paymentStatus")
    @Mapping(source = "booking.uuid", target = "bookingUuid")
    @Mapping(source = "booking.status", target = "bookingStatus")
    @Mapping(source = "booking.showtime.movie.uuid", target = "movieUuid")
    @Mapping(source = "booking.showtime.movie.title", target = "movieTitle")
    @Mapping(source = "booking.showtime.movie.posterPath", target = "posterPath")
    @Mapping(source = "booking.showtime.startTime", target = "showtime")
    @Mapping(source = "booking.showtime.hall.name", target = "hallName")
    PaymentHistoryResponse toPaymentHistoryResponse(Payment payment);

    List<PaymentHistoryResponse> toPaymentHistoryResponseList(List<Payment> payments);
}
