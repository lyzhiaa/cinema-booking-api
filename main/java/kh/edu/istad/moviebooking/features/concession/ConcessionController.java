package kh.edu.istad.moviebooking.features.concession;

import jakarta.validation.Valid;
import kh.edu.istad.moviebooking.features.concession.dto.ConcessionItemResponse;
import kh.edu.istad.moviebooking.features.concession.dto.CreateConcessionRequest;
import kh.edu.istad.moviebooking.features.concession.dto.UpdateConcessionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/concessions")
@RequiredArgsConstructor
public class ConcessionController {

    private final ConcessionService concessionService;
    private final ObjectMapper objectMapper;


    @GetMapping
    public List<ConcessionItemResponse> getAllConcessions() {

        return concessionService.getAllConcessions();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConcessionItemResponse createConcession(
            @Valid
            @RequestBody CreateConcessionRequest createConcessionRequest
    ) {
        return concessionService.createConcession(createConcessionRequest);
    }

    @PatchMapping("/{concessionUuid}")
    public ConcessionItemResponse updateConcession(
            @PathVariable UUID concessionUuid,
            @Valid
            @RequestBody UpdateConcessionRequest updateConcessionRequest
    ) {
        return concessionService.updateConcession(
                concessionUuid,
                updateConcessionRequest
        );
    }

    @GetMapping("/{concessionUuid}")
    public ConcessionItemResponse getConcessionByUuid(@PathVariable UUID concessionUuid) {
        return concessionService.getConcessionByUuid(concessionUuid);
    }

    @DeleteMapping("/{concessionUuid}/permanent")
    public ResponseEntity<Void>
    deleteConcessionPermanently(@PathVariable UUID concessionUuid) {

        concessionService.deleteConcessionPermanently(concessionUuid);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{concessionUuid}/toggle-status")
    public ConcessionItemResponse toggleStatus(@PathVariable UUID concessionUuid) {

        return concessionService.toggleStatus(concessionUuid);
    }
}