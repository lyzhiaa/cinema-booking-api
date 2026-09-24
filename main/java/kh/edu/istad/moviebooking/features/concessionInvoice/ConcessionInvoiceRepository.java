package kh.edu.istad.moviebooking.features.concessionInvoice;

import kh.edu.istad.moviebooking.domain.ConcessionInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConcessionInvoiceRepository
        extends JpaRepository<ConcessionInvoice, Long> {

    Optional<ConcessionInvoice> findByUuid(UUID uuid);

    Optional<ConcessionInvoice>
    findByConcessionOrder_Uuid(UUID concessionOrderUuid);

    Optional<ConcessionInvoice>
    findByQrToken(String qrToken);

    Optional<ConcessionInvoice>
    findByInvoiceNumber(String invoiceNumber);
}