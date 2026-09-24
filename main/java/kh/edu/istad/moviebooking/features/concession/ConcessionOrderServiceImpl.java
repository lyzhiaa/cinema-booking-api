package kh.edu.istad.moviebooking.features.concession;

import kh.edu.istad.moviebooking.domain.Booking;
import kh.edu.istad.moviebooking.domain.ConcessionItem;
import kh.edu.istad.moviebooking.domain.ConcessionOrder;
import kh.edu.istad.moviebooking.domain.ConcessionOrderItem;
import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.domain.enums.BookingStatus;
import kh.edu.istad.moviebooking.domain.enums.ConcessionOrderStatus;
import kh.edu.istad.moviebooking.domain.enums.ConcessionOrderType;
import kh.edu.istad.moviebooking.domain.enums.PaymentStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.auth.CurrentUserService;
import kh.edu.istad.moviebooking.features.booking.BookingRepository;
import kh.edu.istad.moviebooking.features.booking.BookingSeatRepository;
import kh.edu.istad.moviebooking.features.concession.dto.*;
import kh.edu.istad.moviebooking.features.payment.PaymentRepository;
import kh.edu.istad.moviebooking.mapper.ConcessionMapper;
import kh.edu.istad.moviebooking.mapper.ConcessionOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConcessionOrderServiceImpl implements ConcessionOrderService {

    private final ConcessionItemRepository concessionItemRepository;

    private final ConcessionOrderRepository concessionOrderRepository;

    private final BookingRepository bookingRepository;

    private final CurrentUserService currentUserService;

    private final ConcessionMapper concessionMapper;

    private final PaymentRepository paymentRepository;

    private final BookingSeatRepository bookingSeatRepository;

    private final ConcessionOrderMapper concessionOrderMapper;


    @Override
    @Transactional(readOnly = true)
    public List<ConcessionItemResponse> getAvailableItems() {

        List<ConcessionItem> concessionItems = concessionItemRepository.findAllByActiveTrueOrderByCreatedAtDesc();

        return concessionMapper.toConcessionItemResponseList(concessionItems);
    }

    @Override
    @Transactional
    public ConcessionOrderResponse upsertConcessionOrder(UUID bookingUuid, UpsertConcessionOrderRequest request) {

        User currentUser = currentUserService.getCurrentUser();

        Booking booking = bookingRepository.findBookingByUuid(bookingUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        validateBookingOwner(booking, currentUser);

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Concessions can only be changed while booking is pending payment");
        }

        validatePaymentNotCreated(bookingUuid);

        ConcessionOrder order = concessionOrderRepository
                .findByBooking_UuidAndStatus(bookingUuid, ConcessionOrderStatus.PENDING_PAYMENT)
                .orElseGet(() -> ConcessionOrder.builder()
                        .booking(booking)
                        .type(ConcessionOrderType.BOOKING_CHECKOUT)
                        .status(ConcessionOrderStatus.PENDING_PAYMENT)
                        .totalAmount(BigDecimal.ZERO)
                        .build()
                );

        BigDecimal previousConcessionTotal = order.getTotalAmount() == null
                ? BigDecimal.ZERO
                : order.getTotalAmount();

        validateDuplicateItems(request);

        order.getItems().clear();

        BigDecimal newConcessionTotal = BigDecimal.ZERO;

        for (ConcessionOrderItemRequest requestItem
                : request.items()) {

            ConcessionItem concessionItem = concessionItemRepository
                    .findByUuidAndActiveTrue(requestItem.concessionItemUuid())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Concession item not found"));

            BigDecimal unitPrice = concessionItem.getPrice();

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(requestItem.quantity()));

            ConcessionOrderItem orderItem = ConcessionOrderItem.builder()
                    .concessionOrder(order)
                    .concessionItem(concessionItem)
                    .itemName(concessionItem.getName())
                    .quantity(requestItem.quantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(orderItem);

            newConcessionTotal = newConcessionTotal.add(subtotal);
        }

        order.setTotalAmount(newConcessionTotal);

        BigDecimal originalBookingAmount = booking.getTotalAmount().subtract(previousConcessionTotal);

        BigDecimal newBookingTotal = originalBookingAmount.add(newConcessionTotal);

        booking.setTotalAmount(newBookingTotal);

        bookingRepository.save(booking);

        ConcessionOrder savedOrder = concessionOrderRepository.save(order);

        return concessionMapper.toConcessionOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public ConcessionOrderResponse getPendingOrder(UUID bookingUuid) {

        User currentUser = currentUserService.getCurrentUser();

        Booking booking = bookingRepository.findBookingByUuid(bookingUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        validateBookingOwner(booking, currentUser);

        ConcessionOrder order = concessionOrderRepository.findByBooking_UuidAndStatus(bookingUuid, ConcessionOrderStatus.PENDING_PAYMENT)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending concession order found"));

        return concessionMapper.toConcessionOrderResponse(order);
    }

    @Override
    @Transactional
    public void removePendingOrder(UUID bookingUuid) {

        User currentUser = currentUserService.getCurrentUser();

        Booking booking = bookingRepository.findBookingByUuid(bookingUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        validateBookingOwner(booking, currentUser);

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Concession order cannot be removed after payment");
        }

        validatePaymentNotCreated(bookingUuid);

        ConcessionOrder order = concessionOrderRepository
                .findByBooking_UuidAndStatus(bookingUuid, ConcessionOrderStatus.PENDING_PAYMENT)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending concession order found"));

        BigDecimal newBookingTotal = booking.getTotalAmount()
                .subtract(order.getTotalAmount());

        booking.setTotalAmount(newBookingTotal);

        concessionOrderRepository.delete(order);

        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void markPaid(UUID bookingUuid) {

        ConcessionOrder order = concessionOrderRepository
                .findByBooking_UuidAndStatus(bookingUuid, ConcessionOrderStatus.PENDING_PAYMENT)
                .orElse(null);

        if (order == null) {
            return;
        }

        order.setStatus(ConcessionOrderStatus.PAID);

        concessionOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelPendingOrder(UUID bookingUuid) {

        concessionOrderRepository.findByBooking_UuidAndStatus(bookingUuid, ConcessionOrderStatus.PENDING_PAYMENT).ifPresent(order -> {

            order.setStatus(ConcessionOrderStatus.CANCELLED);

            concessionOrderRepository.save(order);
        });
    }

    private void validateBookingOwner(Booking booking, User currentUser) {

        if (!booking.getUser().getUuid().equals(currentUser.getUuid())) {

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this booking");
        }
    }


    private void validateDuplicateItems(UpsertConcessionOrderRequest request) {

        Set<UUID> uniqueItems = new HashSet<>();

        for (ConcessionOrderItemRequest item : request.items()) {

            if (!uniqueItems.add(item.concessionItemUuid())) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate concession item is not allowed");
            }
        }
    }

    @Override
    @Transactional
    public void markPaidByBooking(UUID bookingUuid) {

        concessionOrderRepository.findByBookingUuidAndTypeAndStatus(
                        bookingUuid,
                        ConcessionOrderType.BOOKING_CHECKOUT,
                        ConcessionOrderStatus.PENDING_PAYMENT
                )
                .ifPresent(order -> {
                    order.setStatus(ConcessionOrderStatus.PAID);
                    concessionOrderRepository.save(order);
                });

    }

    private void validatePaymentNotCreated(UUID bookingUuid) {

        boolean paymentCreated = paymentRepository.existsByBookingUuidAndStatus(
                bookingUuid,
                PaymentStatus.PENDING
        );

        if (paymentCreated) {
            throw new BadRequestException("Concession cannot be changed after payment has been created");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EligibleConcessionBookingResponse> getEligibleBookings() {

        User currentUser = currentUserService.getCurrentUser();

        return bookingRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(
                        currentUser.getId(),
                        BookingStatus.CONFIRMED
                )
                .stream()
                .filter(booking ->
                        booking.getShowtime()
                                .getStartTime()
                                .isAfter(LocalDateTime.now())
                )
                .map(booking -> {

                    List<String> seats = bookingSeatRepository.findAllBookingSeatByBookingUuid(
                                    booking.getUuid())
                            .stream()
                            .map(bookingSeat ->
                                    bookingSeat.getSeat().getRowLabel()
                                            + bookingSeat.getSeat().getSeatNumber())
                            .toList();

                    return new EligibleConcessionBookingResponse(
                            booking.getUuid(),
                            booking.getShowtime().getUuid(),
                            booking.getShowtime()
                                    .getMovie()
                                    .getTitle(),
                            booking.getShowtime()
                                    .getHall()
                                    .getName(),
                            booking.getShowtime()
                                    .getStartTime(),
                            seats
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public ConcessionOrderResponse createPostBookingOrder(
            CreatePostBookingConcessionRequest request
    ) {

        User currentUser = currentUserService.getCurrentUser();

        Booking booking = bookingRepository
                .findBookingByUuid(request.bookingUuid())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking",
                                "uuid",
                                request.bookingUuid()
                        )
                );

        // Booking must belong to current user
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException(
                    "You cannot order concessions for this booking"
            );
        }

        // Flow 2 is only available after booking payment succeeded
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException(
                    "Only confirmed bookings can order concessions"
            );
        }

        // Cannot order after movie starts
        if (!booking.getShowtime()
                .getStartTime()
                .isAfter(LocalDateTime.now())) {

            throw new BadRequestException(
                    "Cannot order concessions after the showtime has started"
            );
        }

        // Only one unpaid Flow 2 order at a time
        boolean hasPendingOrder =
                concessionOrderRepository
                        .existsByBookingUuidAndTypeAndStatus(
                                booking.getUuid(),
                                ConcessionOrderType.POST_BOOKING,
                                ConcessionOrderStatus.PENDING_PAYMENT
                        );

        if (hasPendingOrder) {
            throw new BadRequestException("You already have an unpaid concession order for this booking");
        }

        Set<UUID> uniqueItemUuids = new HashSet<>();

        BigDecimal total = BigDecimal.ZERO;

        ConcessionOrder order = ConcessionOrder.builder()
                .booking(booking)
                .type(ConcessionOrderType.POST_BOOKING)
                .status(ConcessionOrderStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        for (ConcessionOrderItemRequest itemRequest : request.items()) {

            if (!uniqueItemUuids.add(itemRequest.concessionItemUuid())) {
                throw new BadRequestException("Duplicate concession item is not allowed");
            }

            ConcessionItem item = concessionItemRepository
                            .findByUuidAndActiveTrue(itemRequest.concessionItemUuid())
                            .orElseThrow(() -> new ResourceNotFoundException("ConcessionItem", "uuid", itemRequest.concessionItemUuid())
                            );

            BigDecimal unitPrice = item.getPrice();

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));

            ConcessionOrderItem orderItem = ConcessionOrderItem.builder()
                            .concessionOrder(order)
                            .concessionItem(item)
                            .itemName(item.getName())
                            .quantity(itemRequest.quantity())
                            .unitPrice(unitPrice)
                            .subtotal(subtotal)
                            .build();

            order.getItems().add(orderItem);

            total = total.add(subtotal);
        }

        order.setTotalAmount(total);

        ConcessionOrder saved = concessionOrderRepository.save(order);

        return concessionOrderMapper.toResponse(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ConcessionOrderResponse> getMyPostBookingOrders() {

        User user = currentUserService.getCurrentUser();

        return concessionOrderRepository.findAllByBookingUserIdAndTypeOrderByCreatedAtDesc(
                        user.getId(),
                        ConcessionOrderType.POST_BOOKING
                )
                .stream()
                .map(concessionOrderMapper::toResponse)
                .toList();
    }


}