package kh.edu.istad.moviebooking.features.concessionInvoice;

import kh.edu.istad.moviebooking.domain.ConcessionInvoice;
import kh.edu.istad.moviebooking.domain.ConcessionOrder;
import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.domain.enums.ConcessionOrderStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.auth.CurrentUserService;
import kh.edu.istad.moviebooking.features.concession.ConcessionOrderRepository;
import kh.edu.istad.moviebooking.features.concessionInvoice.dto.ConcessionInvoiceResponse;
import kh.edu.istad.moviebooking.mapper.ConcessionInvoiceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConcessionInvoiceServiceImpl implements ConcessionInvoiceService {

    private final ConcessionInvoiceRepository concessionInvoiceRepository;

    private final ConcessionOrderRepository concessionOrderRepository;

    private final ConcessionInvoiceMapper concessionInvoiceMapper;
    private final CurrentUserService currentUserService;


    @Override
    @Transactional
    public ConcessionInvoiceResponse generateInvoice(UUID concessionOrderUuid) {

        ConcessionOrder order = concessionOrderRepository.findConcessionOrderByUuid(concessionOrderUuid)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "ConcessionOrder",
                                        "uuid",
                                        concessionOrderUuid
                                )
                        );

        if (order.getStatus() != ConcessionOrderStatus.PAID) {
            throw new BadRequestException("Invoice can only be generated after payment");
        }

        Optional<ConcessionInvoice> existing = concessionInvoiceRepository.findByConcessionOrder_Uuid(concessionOrderUuid);

        if (existing.isPresent()) {return concessionInvoiceMapper
                    .toResponse(existing.get());
        }

        ConcessionInvoice invoice = ConcessionInvoice.builder()
                        .concessionOrder(order)
                        .invoiceNumber(generateInvoiceNumber())
                        .qrToken(UUID.randomUUID().toString())
                        .issuedAt(LocalDateTime.now())
                        .build();

        ConcessionInvoice saved = concessionInvoiceRepository.save(invoice);

        return concessionInvoiceMapper
                .toResponse(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public ConcessionInvoiceResponse getInvoiceByOrder(
            UUID concessionOrderUuid
    ) {

        ConcessionInvoice invoice = concessionInvoiceRepository
                        .findByConcessionOrder_Uuid(concessionOrderUuid)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "ConcessionInvoice",
                                        "concessionOrderUuid",
                                        concessionOrderUuid
                                )
                        );

        User currentUser = currentUserService.getCurrentUser();

        if (!invoice.getConcessionOrder()
                .getBooking()
                .getUser()
                .getId()
                .equals(currentUser.getId())) {

            throw new BadRequestException("You cannot access this concession invoice");
        }

        return concessionInvoiceMapper.toResponse(invoice);
    }


    @Override
    @Transactional
    public ConcessionInvoiceResponse pickup(String qrToken) {

        ConcessionInvoice invoice = concessionInvoiceRepository.findByQrToken(qrToken)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "ConcessionInvoice",
                                        "qrToken",
                                        qrToken
                                )
                        );

        /*
         * Prevent the same invoice from being
         * used twice.
         */
        if (invoice.getPickedUpAt() != null) {

            throw new BadRequestException("This concession has already been picked up");
        }

        /*
         * Extra safety:
         * invoice's order must still be paid.
         */
        if (invoice.getConcessionOrder().getStatus() != ConcessionOrderStatus.PAID) {

            throw new BadRequestException("This concession order has not been paid");
        }

        invoice.setPickedUpAt(LocalDateTime.now());

        ConcessionInvoice saved = concessionInvoiceRepository.save(invoice);

        return concessionInvoiceMapper.toResponse(saved);
    }


    private String generateInvoiceNumber() {

        return "CON-"
                + LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();
    }


}