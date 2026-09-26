package kh.edu.istad.moviebooking.features.groupPayment;

import kh.edu.istad.moviebooking.domain.*;
import kh.edu.istad.moviebooking.domain.enums.*;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.auth.CurrentUserService;
import kh.edu.istad.moviebooking.features.bakong.BakongClient;
import kh.edu.istad.moviebooking.features.bakong.BakongKhqrService;
import kh.edu.istad.moviebooking.features.bakong.dto.BakongCheckTransactionResponse;
import kh.edu.istad.moviebooking.features.bakong.dto.BakongKhqrResult;
import kh.edu.istad.moviebooking.features.bakong.dto.BakongTransactionData;
import kh.edu.istad.moviebooking.features.booking.BookingRepository;
import kh.edu.istad.moviebooking.features.concession.ConcessionOrderService;
import kh.edu.istad.moviebooking.features.groupBooking.GroupBookingRepository;
import kh.edu.istad.moviebooking.features.groupBooking.GroupMemberRepository;
import kh.edu.istad.moviebooking.features.groupPayment.dto.GroupPaymentResponse;
import kh.edu.istad.moviebooking.features.qrcode.QrCodeService;
import kh.edu.istad.moviebooking.features.ticket.TicketService;
import kh.edu.istad.moviebooking.intergration.bakong.BakongProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupPaymentServiceImpl implements GroupPaymentService {

    private final GroupPaymentRepository groupPaymentRepository;

    private final GroupBookingRepository groupBookingRepository;

    private final GroupMemberRepository groupMemberRepository;

    private final BookingRepository bookingRepository;

    private final CurrentUserService currentUserService;

    private final BakongKhqrService bakongKhqrService;

    private final BakongClient bakongClient;

    private final BakongProperties bakongProperties;

    private final TicketService ticketService;

    private final ConcessionOrderService concessionOrderService;

    private final QrCodeService qrCodeService;

    @Override
    @Transactional
    public GroupPaymentResponse createPayment(
            UUID groupUuid
    ) {

        // 1. Current user must be the host
        User currentUser =
                currentUserService.getCurrentUser();

        // 2. Load and lock group
        GroupBooking group =
                groupBookingRepository
                        .findByUuidForUpdate(groupUuid)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "GroupBooking",
                                        "uuid",
                                        groupUuid
                                )
                        );

        // 3. Only host can create group payment
        if (!group.getHost()
                .getUuid()
                .equals(currentUser.getUuid())) {

            throw new BadRequestException(
                    "Only the group host can create payment"
            );
        }

        /*
         * 4. Important:
         *
         * If payment already exists, return it.
         *
         * This makes:
         *
         * POST /group-bookings/{groupUuid}/payments
         *
         * safe to call again.
         */
        GroupPayment existingPayment =
                groupPaymentRepository
                        .findByGroupBookingUuid(groupUuid)
                        .orElse(null);

        if (existingPayment != null) {
            return toResponse(existingPayment);
        }

        /*
         * 5. If group says PAYMENT_PENDING
         * but no GroupPayment exists,
         * database state is inconsistent.
         */
        if (group.getStatus()
                == GroupBookingStatus.PAYMENT_PENDING) {

            throw new BadRequestException(
                    "Group is waiting for payment but payment record was not found"
            );
        }

        // 6. Group must be locked first
        if (group.getStatus()
                != GroupBookingStatus.LOCKED) {

            throw new BadRequestException(
                    "Group must be locked before creating payment"
            );
        }

        // 7. Payment deadline must exist
        if (group.getPaymentExpiresAt() == null) {

            throw new BadRequestException(
                    "Group payment deadline is missing"
            );
        }

        // 8. Payment deadline must not be expired
        if (group.getPaymentExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "Group payment deadline has passed"
            );
        }

        // 9. Get all active members
        List<GroupMember> members =
                groupMemberRepository
                        .findAllByGroupBookingUuidAndStatusNot(
                                groupUuid,
                                GroupMemberStatus.LEFT
                        );

        if (members.isEmpty()) {

            throw new BadRequestException(
                    "Group has no members"
            );
        }

        /*
         * 10. Every active member must have a booking.
         *
         * Host does not need READY before lock,
         * but host still must have a Booking.
         */
        for (GroupMember member : members) {

            if (member.getBooking() == null) {

                throw new BadRequestException(
                        "All group members must have a booking"
                );
            }

            if (member.getBooking().getStatus()
                    != BookingStatus.PENDING_PAYMENT) {

                throw new BadRequestException(
                        "All member bookings must be waiting for payment"
                );
            }
        }

        // 11. Calculate total from backend Booking data
        BigDecimal totalAmount =
                members.stream()
                        .map(GroupMember::getBooking)
                        .map(Booking::getTotalAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        if (totalAmount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new BadRequestException(
                    "Group payment amount must be greater than zero"
            );
        }

        // 12. Generate bill number
        String billNumber =
                "GROUP-" +
                        group.getUuid()
                                .toString()
                                .substring(0, 12);

        // 13. Generate KHQR
        BakongKhqrResult khqr =
                bakongKhqrService.generateKhqr(
                        totalAmount,
                        group.getPaymentExpiresAt(),
                        billNumber
                );

        // 14. Create GroupPayment
        GroupPayment payment =
                GroupPayment.builder()
                        .uuid(UUID.randomUUID())
                        .groupBooking(group)
                        .amount(totalAmount)
                        .status(
                                PaymentStatus.PENDING
                        )
                        .paymentMethod(
                                PaymentMethod.KHQR
                        )
                        .providerReference(
                                khqr.md5()
                        )
                        .qrPayload(
                                khqr.qr()
                        )
                        .build();

        /*
         * 15. Save payment first.
         *
         * saveAndFlush makes PostgreSQL
         * insert it immediately.
         */
        groupPaymentRepository
                .saveAndFlush(payment);

        // 16. Now move group to PAYMENT_PENDING
        group.setStatus(
                GroupBookingStatus.PAYMENT_PENDING
        );

        groupBookingRepository
                .saveAndFlush(group);

        // 17. Return created payment
        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupPaymentResponse getPayment(UUID paymentUuid) {

        GroupPayment payment = groupPaymentRepository
                        .findByUuid(paymentUuid)
                        .orElseThrow(() -> new ResourceNotFoundException("GroupPayment", "uuid", paymentUuid));

        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getQrImage(UUID paymentUuid) {

        GroupPayment payment = groupPaymentRepository.findByUuid(paymentUuid)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "GroupPayment",
                                        "uuid",
                                        paymentUuid
                                )
                        );

        return qrCodeService.generateQrCode(payment.getQrPayload(), 400, 400);
    }

    @Override
    @Transactional
    public GroupPaymentResponse verifyPayment(UUID paymentUuid) {

        GroupPayment payment = groupPaymentRepository.findByUuid(paymentUuid)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "GroupPayment",
                                        "uuid",
                                        paymentUuid
                                )
                        );

        if (payment.getStatus() == PaymentStatus.SUCCESS) {

            return toResponse(payment);
        }

        GroupBooking group = payment.getGroupBooking();

        if (group.getStatus() != GroupBookingStatus.PAYMENT_PENDING) {

            throw new BadRequestException("Group is not waiting for payment");
        }

        if (group.getPaymentExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException("Group payment deadline has passed");
        }

        BakongCheckTransactionResponse response = bakongClient
                        .checkTransactionByMd5(payment.getProviderReference());

        if (response == null || response.responseCode() == null || response.responseCode() != 0 || response.data() == null) {

            return toResponse(payment);
        }

        BakongTransactionData transaction = response.data();

        if (transaction.amount()
                .compareTo(payment.getAmount()) != 0) {

            throw new BadRequestException("Payment amount does not match");
        }

        if (!bakongProperties.getAccountId().equalsIgnoreCase(transaction.toAccountId())) {

            throw new BadRequestException("Payment receiver does not match");
        }

        if (!bakongProperties.getCurrency().equalsIgnoreCase(transaction.currency())) {

            throw new BadRequestException("Payment currency does not match");
        }

        List<GroupMember> members = groupMemberRepository
                        .findAllByGroupBookingUuidAndStatusNot(
                                group.getUuid(),
                                GroupMemberStatus.LEFT
                        );

        payment.setStatus(PaymentStatus.SUCCESS);

        payment.setTransactionReference(transaction.hash());

        payment.setPaidAt(LocalDateTime.now());

        group.setStatus(GroupBookingStatus.CONFIRMED);

        for (GroupMember member : members) {

            Booking booking = member.getBooking();

            if (booking == null) {
                throw new BadRequestException("Group member does not have a booking");
            }

            booking.setStatus(BookingStatus.CONFIRMED);

            member.setStatus(GroupMemberStatus.CONFIRMED);
        }

        bookingRepository.saveAllAndFlush(members.stream()
                        .map(GroupMember::getBooking)
                        .toList()
        );

        groupMemberRepository.saveAll(members);

        for (GroupMember member : members) {

            Booking booking = member.getBooking();

            concessionOrderService.markPaidByBooking(booking.getUuid());

            ticketService.generateTicketsForBooking(booking.getUuid());
        }

        groupPaymentRepository.save(payment);

        groupBookingRepository.save(group);

        return toResponse(payment);
    }

    private GroupPaymentResponse toResponse(
            GroupPayment payment
    ) {

        return new GroupPaymentResponse(
                payment.getUuid(),
                payment.getGroupBooking()
                        .getUuid(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getPaidAt()
        );
    }

}
