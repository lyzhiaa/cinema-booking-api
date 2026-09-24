package kh.edu.istad.moviebooking.features.concession;

import kh.edu.istad.moviebooking.features.concession.dto.*;

import java.util.List;
import java.util.UUID;

public interface ConcessionOrderService {

    List<ConcessionItemResponse> getAvailableItems();

    ConcessionOrderResponse upsertConcessionOrder(UUID bookingUuid, UpsertConcessionOrderRequest request);

    ConcessionOrderResponse getPendingOrder(UUID bookingUuid);

    void removePendingOrder(UUID bookingUuid);

    void markPaid(UUID bookingUuid);

    void cancelPendingOrder(UUID bookingUuid);

    void markPaidByBooking(UUID bookingUuid);

    List<EligibleConcessionBookingResponse> getEligibleBookings();

    ConcessionOrderResponse createPostBookingOrder(
            CreatePostBookingConcessionRequest createPostBookingConcessionRequest
    );

    List<ConcessionOrderResponse> getMyPostBookingOrders();


}