package kh.edu.istad.moviebooking.features.concessionInvoice;

import jakarta.validation.Valid;
import kh.edu.istad.moviebooking.features.concessionInvoice.dto.ConcessionInvoiceResponse;
import kh.edu.istad.moviebooking.features.concessionInvoice.dto.ConcessionPickupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/concession-invoices")
@RequiredArgsConstructor
public class ConcessionInvoiceController {

    private final ConcessionInvoiceService concessionInvoiceService;


    @GetMapping("/orders/{orderUuid}")
    public ConcessionInvoiceResponse getInvoiceByOrder(@PathVariable UUID orderUuid) {

        return concessionInvoiceService.getInvoiceByOrder(orderUuid);
    }


    @PostMapping("/pickup")
    public ConcessionInvoiceResponse pickup(
            @Valid
            @RequestBody ConcessionPickupRequest concessionPickupRequest
    ) {
        return concessionInvoiceService.pickup(concessionPickupRequest.qrToken());
    }

}