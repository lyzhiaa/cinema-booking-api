package kh.edu.istad.moviebooking.features.hall;

import kh.edu.istad.moviebooking.features.hall.dto.CreateHallRequest;
import kh.edu.istad.moviebooking.features.hall.dto.HallResponse;
import kh.edu.istad.moviebooking.features.hall.dto.HallUpdateRequest;
import kh.edu.istad.moviebooking.features.hall.dto.UpdateHallStatusRequest;

import java.util.List;
import java.util.UUID;

public interface HallService {
//    create hall
    HallResponse createHall(CreateHallRequest createHallRequest);
//    Get all halls
    List<HallResponse> getAllHalls();
//    Get hall by uuid
    HallResponse getHallByUuid(UUID uuid);
//    update hall status
    HallResponse updateHallStatus(UUID uuid, UpdateHallStatusRequest updateHallStatusRequest);
//    delete hall
    void deleteHall(UUID uuid);
//    update hall capacity
    HallResponse updateHallCapacity(UUID uuid, HallUpdateRequest hallUpdateRequest);
}
