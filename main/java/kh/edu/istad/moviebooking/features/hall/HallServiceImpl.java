package kh.edu.istad.moviebooking.features.hall;

import jakarta.transaction.Transactional;
import kh.edu.istad.moviebooking.domain.Hall;
import kh.edu.istad.moviebooking.exception.ResourceAlreadyExistsException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.hall.dto.CreateHallRequest;
import kh.edu.istad.moviebooking.features.hall.dto.HallResponse;
import kh.edu.istad.moviebooking.features.hall.dto.HallUpdateRequest;
import kh.edu.istad.moviebooking.features.hall.dto.UpdateHallStatusRequest;
import kh.edu.istad.moviebooking.features.seat.SeatRepository;
import kh.edu.istad.moviebooking.mapper.HallMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HallServiceImpl implements HallService {
    private final HallRepository hallRepository;
    private final HallMapper hallMapper;
    private final SeatRepository seatRepository;

    //    create hall
    @Override
    public HallResponse createHall(CreateHallRequest createHallRequest) {
//        check if hall is already exist
        if(hallRepository.existsByNameIgnoreCase(createHallRequest.name())) {
            throw new ResourceAlreadyExistsException(
                    "Hall",
                    "name",
                    createHallRequest.name()
            );
        }
        Hall hall = hallMapper.fromCreateHallRequest(createHallRequest);
        hallRepository.save(hall);

        return hallMapper.toHallResponse(hall);
    }

//    get all halls
    @Override
    public List<HallResponse> getAllHalls() {
        List<Hall> halls = hallRepository.findAll();
        return hallMapper.toHallResponseList(halls);
    }

//    get hall by uuid
    @Override
    public HallResponse getHallByUuid(UUID uuid) {
        Hall hall = hallRepository.findHallByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Hall", "uuid", uuid));

        return hallMapper.toHallResponse(hall);
    }

//    update hall status
    @Override
    @Transactional
    public HallResponse updateHallStatus(
            UUID uuid,
            UpdateHallStatusRequest request
    ) {

        Hall hall = hallRepository
                .findHallByUuid(uuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hall",
                                "uuid",
                                uuid
                        )
                );

        hall.setStatus(request.status());

        return hallMapper.toHallResponse(hall);
    }

//    TODO: delete hall
    @Override
    public void deleteHall(UUID uuid) {
        Hall hall = hallRepository
                .findHallByUuid(uuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hall",
                                "uuid",
                                uuid
                        )
                );
        hallRepository.delete(hall);
    }

//    update hall capacity
    @Override
    public HallResponse updateHallCapacity(UUID uuid, HallUpdateRequest hallUpdateRequest) {
        Hall hall = hallRepository
                .findHallByUuid(uuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hall",
                                "uuid",
                                uuid
                        )
                );

        hallMapper.fromHallUpdateRequest(hallUpdateRequest, hall);
        hallRepository.save(hall);
        return hallMapper.toHallResponse(hall);
    }

}
