package kh.edu.istad.moviebooking.features.hall;

import kh.edu.istad.moviebooking.features.hall.dto.CreateHallRequest;
import kh.edu.istad.moviebooking.features.hall.dto.HallResponse;
import kh.edu.istad.moviebooking.features.hall.dto.UpdateHallRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    @PostMapping
    public HallResponse createHall(
            @RequestBody CreateHallRequest createHallRequest
    ) {
        return hallService.createHall(createHallRequest);
    }

    @GetMapping
    public List<HallResponse> getAllHalls() {
        return hallService.getAllHalls();
    }

    @GetMapping("/{uuid}")
    public HallResponse getHallByUuid(
            @PathVariable UUID uuid
    ) {
        return hallService.getHallByUuid(uuid);
    }

    @PatchMapping("/{uuid}/status")
    public HallResponse updateStatus(
            @PathVariable UUID uuid,
            @RequestBody UpdateHallRequest updateHallRequest
    ) {
        return hallService.updateHallStatus(
                uuid,
                updateHallRequest
        );
    }
//    delete hall
    @DeleteMapping("/{uuid}")
    void deleteHall(@PathVariable("uuid") UUID uuid) {
        hallService.deleteHall(uuid);
    }
}
