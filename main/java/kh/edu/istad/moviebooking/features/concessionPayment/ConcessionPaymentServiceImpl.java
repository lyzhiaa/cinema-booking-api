package kh.edu.istad.moviebooking.features.concessionPayment;

import kh.edu.istad.moviebooking.domain.ConcessionOrder;
import kh.edu.istad.moviebooking.domain.ConcessionPayment;
import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.domain.enums.ConcessionOrderStatus;
import kh.edu.istad.moviebooking.domain.enums.ConcessionOrderType;
import kh.edu.istad.moviebooking.domain.enums.PaymentMethod;
import kh.edu.istad.moviebooking.domain.enums.PaymentStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.auth.CurrentUserService;
import kh.edu.istad.moviebooking.features.bakong.BakongClient;
import kh.edu.istad.moviebooking.features.bakong.BakongKhqrService;
import kh.edu.istad.moviebooking.features.bakong.dto.BakongCheckTransactionResponse;
import kh.edu.istad.moviebooking.features.bakong.dto.BakongKhqrResult;
import kh.edu.istad.moviebooking.features.bakong.dto.BakongTransactionData;
import kh.edu.istad.moviebooking.features.concession.ConcessionOrderRepository;
import kh.edu.istad.moviebooking.features.concessionInvoice.ConcessionInvoiceService;
import kh.edu.istad.moviebooking.features.concessionPayment.dto.ConcessionPaymentResponse;
import kh.edu.istad.moviebooking.intergration.bakong.BakongProperties;
import kh.edu.istad.moviebooking.mapper.ConcessionPaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConcessionPaymentServiceImpl implements ConcessionPaymentService {
    private final CurrentUserService currentUserService;
    private final ConcessionPaymentRepository concessionPaymentRepository;
    private final ConcessionOrderRepository concessionOrderRepository;
    private final BakongKhqrService bakongKhqrService;
    private final ConcessionPaymentMapper concessionPaymentMapper;

    private final ConcessionInvoiceService concessionInvoiceService;

    private final BakongClient bakongClient;
    private final BakongProperties bakongProperties;

    @Override
    @Transactional
    public ConcessionPaymentResponse createPayment(UUID concessionOrderUuid) {

        User currentUser = currentUserService.getCurrentUser();

        ConcessionOrder order = concessionOrderRepository.findConcessionOrderByUuid(concessionOrderUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "ConcessionOrder",
                                "orderUuid",
                                concessionOrderUuid
                        )
                );

        if (!order.getBooking()
                .getUser()
                .getId()
                .equals(currentUser.getId())) {

            throw new BadRequestException("You cannot pay for this concession order");
        }

        if (order.getType() != ConcessionOrderType.POST_BOOKING) {
            throw new BadRequestException("This payment endpoint is only for post-booking concession orders");
        }

        if (order.getStatus() != ConcessionOrderStatus.PENDING_PAYMENT) {

            throw new BadRequestException("Concession order is not awaiting payment");
        }

        Optional<ConcessionPayment> existing = concessionPaymentRepository.findByConcessionOrder_Uuid(concessionOrderUuid);

        if (existing.isPresent() && existing.get().getStatus() == PaymentStatus.PENDING) {

            return concessionPaymentMapper.toResponse(existing.get());
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        String billNumber = "CON-"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        BakongKhqrResult khqr = bakongKhqrService.generateKhqr(
                order.getTotalAmount(),
                expiresAt,
                billNumber
        );

        ConcessionPayment payment = ConcessionPayment.builder()
                .concessionOrder(order)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.KHQR)
                .billNumber(billNumber)
                .expiresAt(expiresAt)
                .providerReference(khqr.md5())
                .qrPayload(khqr.qr())
                .build();

        payment = concessionPaymentRepository.save(payment);

        return concessionPaymentMapper
                .toResponse(payment);
    }

    @Override
    @Transactional
    public ConcessionPaymentResponse verifyPayment(UUID paymentUuid) {

        ConcessionPayment payment = concessionPaymentRepository.findByUuid(paymentUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "ConcessionPayment",
                                "uuid",
                                paymentUuid
                        )
                );

        ConcessionOrder order = payment.getConcessionOrder();

        User currentUser = currentUserService.getCurrentUser();

        // Make sure this concession order belongs to current user
        if (!order.getBooking()
                .getUser()
                .getId()
                .equals(currentUser.getId())) {

            throw new BadRequestException("You cannot verify this concession payment");
        }

        // Already successful
        if (payment.getStatus() == PaymentStatus.SUCCESS) {

            // Safe because generateInvoice() prevents duplicates
            concessionInvoiceService.generateInvoice(order.getUuid());

            return concessionPaymentMapper.toResponse(payment);
        }

        // Payment QR expired
        if (payment.getExpiresAt() != null && payment.getExpiresAt().isBefore(LocalDateTime.now())) {

            throw new BadRequestException("Concession payment has expired");
        }

        // Check transaction with Bakong
        BakongCheckTransactionResponse response = bakongClient.checkTransactionByMd5(
                payment.getProviderReference());

        // Customer has not paid yet
        if (response == null
                || response.responseCode() == null
                || response.responseCode() != 0
                || response.data() == null) {

            return concessionPaymentMapper
                    .toResponse(payment);
        }

        BakongTransactionData transaction = response.data();

        // Validate amount
        if (transaction.amount().compareTo(payment.getAmount()) != 0) {

            throw new BadRequestException("Payment amount does not match");
        }

        // Validate receiver
        if (!bakongProperties
                .getAccountId()
                .equalsIgnoreCase(
                        transaction.toAccountId()
                )) {

            throw new BadRequestException("Payment receiver does not match");
        }

        // Validate currency
        if (!bakongProperties.getCurrency().equalsIgnoreCase(transaction.currency())) {

            throw new BadRequestException("Payment currency does not match");
        }

        // -----------------------------
        // PAYMENT SUCCESS
        // -----------------------------

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());


        order.setStatus(ConcessionOrderStatus.PAID);

        concessionPaymentRepository.saveAndFlush(payment);
        concessionOrderRepository.saveAndFlush(order);

        concessionInvoiceService.generateInvoice(
                order.getUuid()
        );

        return concessionPaymentMapper.toResponse(payment);
    }
}
