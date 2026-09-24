package kh.edu.istad.moviebooking.features.concession;

import kh.edu.istad.moviebooking.features.concession.dto.ConcessionItemResponse;
import kh.edu.istad.moviebooking.features.concession.dto.ConcessionOrderResponse;
import kh.edu.istad.moviebooking.features.concession.dto.CreateConcessionRequest;
import kh.edu.istad.moviebooking.features.concession.dto.UpdateConcessionRequest;

import java.util.List;
import java.util.UUID;

public interface ConcessionService {
    List<ConcessionItemResponse> getAllConcessions();

    ConcessionItemResponse createConcession(CreateConcessionRequest request);

    ConcessionItemResponse updateConcession(UUID concessionUuid, UpdateConcessionRequest request);

    ConcessionItemResponse getConcessionByUuid(UUID concessionUuid);

    void deleteConcessionPermanently(UUID concessionUuid);

    ConcessionItemResponse toggleStatus(UUID concessionUuid);


}
