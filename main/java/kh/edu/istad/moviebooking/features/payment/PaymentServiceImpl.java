package kh.edu.istad.moviebooking.features.payment;

import kh.edu.istad.moviebooking.domain.Booking;
import kh.edu.istad.moviebooking.domain.Payment;
import kh.edu.istad.moviebooking.domain.enums.BookingStatus;
import kh.edu.istad.moviebooking.domain.enums.PaymentMethod;
import kh.edu.istad.moviebooking.domain.enums.PaymentStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.booking.BookingRepository;
import kh.edu.istad.moviebooking.features.payment.dto.PaymentResponse;
import kh.edu.istad.moviebooking.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
//    mapper
    private final PaymentMapper paymentMapper;


    @Override
    @Transactional
    public PaymentResponse createPayment(UUID bookingUuid) {
//        find booking
        Booking booking = bookingRepository.findBookingByUuid(bookingUuid).orElseThrow(
                () -> new ResourceNotFoundException("Booking", "uuid", bookingUuid)
        );

        if(booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Booking is not waiting for payment");
        }
        // Check whether payment time already passed
        if (
                booking.getPaymentExpiresAt() != null
                        && booking.getPaymentExpiresAt()
                        .isBefore(LocalDateTime.now())
        ) {
            throw new BadRequestException(
                    "Booking has expired"
            );
        }
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalAmount())
                .paymentMethod(PaymentMethod.KHQR)
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);
        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse markPaymentSuccess(UUID paymentUuid) {

        Payment payment = paymentRepository.findPaymentByUuid(paymentUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "uuid", paymentUuid));

        Booking booking = payment.getBooking();

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException(
                    "Payment is already successful"
            );
        }

        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BadRequestException(
                    "Cannot pay for an expired booking"
            );
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Booking is not waiting for payment");
        }

        if (booking.getPaymentExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Payment deadline has passed");
        }

        payment.setStatus(PaymentStatus.SUCCESS);

        payment.setTransactionReference("TEST-" + UUID.randomUUID());

        booking.setStatus(BookingStatus.CONFIRMED);

        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByUuid(UUID paymentUuid) {
        Payment payment = paymentRepository.findPaymentByUuid(paymentUuid).orElseThrow(
                () -> new ResourceNotFoundException("Payment", "uuid", paymentUuid));

        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse markPaymentFailed(UUID paymentUuid) {
        Payment payment = paymentRepository.findPaymentByUuid(paymentUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Payment",
                                "uuid",
                                paymentUuid
                        )
                );

        Booking booking = payment.getBooking();

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException("Successful payment cannot be marked as failed");
        }

        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BadRequestException("Booking has already expired");
        }

        payment.setStatus(PaymentStatus.FAILED);

        return paymentMapper.toPaymentResponse(payment);
    }
}
