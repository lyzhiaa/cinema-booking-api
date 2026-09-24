package kh.edu.istad.moviebooking.features.concessionInvoice;

import kh.edu.istad.moviebooking.features.concessionInvoice.dto.ConcessionInvoiceResponse;
import kh.edu.istad.moviebooking.features.concessionPayment.dto.ConcessionPaymentResponse;

import java.util.UUID;

public interface ConcessionInvoiceService {

    ConcessionInvoiceResponse generateInvoice(UUID concessionOrderUuid);

    ConcessionInvoiceResponse getInvoiceByOrder(UUID concessionOrderUuid);

    ConcessionInvoiceResponse pickup(String qrToken);

}
