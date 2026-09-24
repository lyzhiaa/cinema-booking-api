package kh.edu.istad.moviebooking.features.payment;

import kh.edu.istad.moviebooking.domain.Booking;
import kh.edu.istad.moviebooking.domain.Payment;
import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.domain.enums.BookingStatus;
import kh.edu.istad.moviebooking.domain.enums.PaymentMethod;
import kh.edu.istad.moviebooking.domain.enums.PaymentStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.auth.CurrentUserService;
import kh.edu.istad.moviebooking.features.bakong.BakongClient;
import kh.edu.istad.moviebooking.features.bakong.dto.BakongCheckTransactionResponse;
import kh.edu.istad.moviebooking.features.bakong.dto.BakongKhqrResult;
import kh.edu.istad.moviebooking.features.bakong.BakongKhqrService;
import kh.edu.istad.moviebooking.features.bakong.dto.BakongTransactionData;
import kh.edu.istad.moviebooking.features.booking.BookingRepository;
import kh.edu.istad.moviebooking.features.common.PageResponse;
import kh.edu.istad.moviebooking.features.concession.ConcessionOrderService;
import kh.edu.istad.moviebooking.features.payment.dto.PaymentHistoryResponse;
import kh.edu.istad.moviebooking.features.payment.dto.PaymentResponse;
import kh.edu.istad.moviebooking.features.ticket.TicketService;
import kh.edu.istad.moviebooking.intergration.bakong.BakongProperties;
import kh.edu.istad.moviebooking.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
//    mapper
    private final PaymentMapper paymentMapper;

    private final TicketService ticketService;

    private final BakongKhqrService bakongKhqrService;
    private final BakongClient bakongClient;
    private final BakongProperties bakongProperties;

    private final CurrentUserService currentUserService;

    private final ConcessionOrderService concessionOrderService;


    @Override
    @Transactional
    public PaymentResponse createPayment(UUID bookingUuid) {

        Booking booking = bookingRepository.findBookingByUuid(bookingUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Booking",
                                "uuid",
                                bookingUuid
                        )
                );

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BadRequestException(
                    "Booking is not waiting for payment"
            );
        }

        if (booking.getPaymentExpiresAt() != null && booking.getPaymentExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Booking has expired");
        }

        // Add it HERE
        String billNumber = "BOOK-" + booking.getUuid()
                                .toString()
                                .substring(0, 12);

        // Then use it here
        BakongKhqrResult khqr = bakongKhqrService.generateKhqr(
                        booking.getTotalAmount(),
                        booking.getPaymentExpiresAt(),
                        billNumber
                );

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.KHQR)
                .providerReference(khqr.md5())
                .qrPayload(khqr.qr())
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
            throw new BadRequestException("Payment is already successful");
        }

        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BadRequestException("Cannot pay for an expired booking");
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Booking is not waiting for payment");
        }

        if (booking.getPaymentExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Payment deadline has passed");
        }

        payment.setStatus(PaymentStatus.SUCCESS);

        payment.setTransactionReference(
                "TEST-" + UUID.randomUUID()
        );

        payment.setPaidAt(LocalDateTime.now());

        booking.setStatus(BookingStatus.CONFIRMED);

        concessionOrderService.markPaidByBooking(
                booking.getUuid()
        );

        awardPoints(payment, booking);

        ticketService.generateTicketsForBooking(
                booking.getUuid()
        );

        paymentRepository.saveAndFlush(payment);
        bookingRepository.saveAndFlush(booking);

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

    @Override
    @Transactional
    public PaymentResponse verifyBakongPayment(UUID paymentUuid) {

        Payment payment = paymentRepository.findPaymentByUuid(paymentUuid)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "Payment",
                                        "uuid",
                                        paymentUuid
                                )
                        );

        Booking booking = payment.getBooking();

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return paymentMapper.toPaymentResponse(payment);
        }

        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BadRequestException("Booking has expired");
        }

        BakongCheckTransactionResponse response = bakongClient.checkTransactionByMd5(payment.getProviderReference());

        // Not paid yet
        if (response == null || response.responseCode() == null || response.responseCode() != 0 || response.data() == null) {

            return paymentMapper.toPaymentResponse(payment);
        }

        BakongTransactionData transaction = response.data();

        // IMPORTANT: validate amount
        if (transaction.amount().compareTo(payment.getAmount()) != 0) {
            throw new BadRequestException("Payment amount does not match");
        }

        // Validate receiver
        if (!bakongProperties.getAccountId().equalsIgnoreCase(transaction.toAccountId())) {
            throw new BadRequestException("Payment receiver does not match");
        }

        // Validate currency
        if (!bakongProperties.getCurrency().equalsIgnoreCase(transaction.currency())) {
            throw new BadRequestException("Payment currency does not match");
        }

        payment.setStatus(PaymentStatus.SUCCESS);

        payment.setTransactionReference(transaction.hash());

        payment.setPaidAt(LocalDateTime.now());

        booking.setStatus(BookingStatus.CONFIRMED);

        concessionOrderService.markPaidByBooking(booking.getUuid());

        awardPoints(payment, booking);

        ticketService.generateTicketsForBooking(booking.getUuid());

        paymentRepository.saveAndFlush(payment);

        bookingRepository.saveAndFlush(booking);

        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentHistoryResponse> getMyPayments(int page, int size) {

        User user = currentUserService.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Payment> paymentPage = paymentRepository.findAllByBookingUserUuid(
                                user.getUuid(),
                                pageable
                        );

        List<PaymentHistoryResponse> payments = paymentMapper
                        .toPaymentHistoryResponseList(paymentPage.getContent());

        return new PageResponse<>(
                payments,
                paymentPage.getNumber(),
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages(),
                paymentPage.isFirst(),
                paymentPage.isLast()
        );
    }

    private void awardPoints(Payment payment, Booking booking) {

        if (Boolean.TRUE.equals(booking.getPointsAwarded())) {
            return;
        }

        int earnedPoints = payment.getAmount()
                        .setScale(0, RoundingMode.FLOOR)
                        .intValueExact();

        User user = booking.getUser();

        int currentPoints = user.getPoints() == null
                        ? 0
                        : user.getPoints();

        user.setPoints(currentPoints + earnedPoints);

        booking.setPointsAwarded(true);
    }
}
