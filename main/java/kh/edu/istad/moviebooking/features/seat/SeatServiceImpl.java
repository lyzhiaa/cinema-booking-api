package kh.edu.istad.moviebooking.features.seat;

import jakarta.transaction.Transactional;
import kh.edu.istad.moviebooking.domain.Hall;
import kh.edu.istad.moviebooking.domain.Seat;
import kh.edu.istad.moviebooking.exception.ResourceAlreadyExistsException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.hall.HallRepository;
import kh.edu.istad.moviebooking.features.seat.dto.CreateSeatRequest;
import kh.edu.istad.moviebooking.features.seat.dto.SeatResponse;
import kh.edu.istad.moviebooking.features.seat.dto.UpdateSeatStatusRequest;
import kh.edu.istad.moviebooking.mapper.SeatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {
    private final SeatRepository seatRepository;
    private final HallRepository hallRepository;
    private final SeatMapper seatMapper;

    @Override
    @Transactional
    public SeatResponse createSeat(UUID hallUuid, CreateSeatRequest createSeatRequest) {
//        find hall before create seat
        Hall hall = hallRepository
                .findHallByUuid(hallUuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hall",
                                "uuid",
                                hallUuid
                        )
                );
//        convert row label and seat label to uppercase
        String rowLabel = createSeatRequest.rowLabel().trim().toUpperCase();

        String seatLabel = rowLabel + createSeatRequest.seatNumber();

//        check if seats are already created
        if (seatRepository.existsByHallUuidAndSeatLabel(hallUuid, seatLabel)) {
            throw new ResourceAlreadyExistsException(
                    "Seat",
                    "label",
                    seatLabel
            );
        }
//        create seat
        Seat seat = seatMapper.fromCreateSeatRequest(createSeatRequest);
        seat.setHall(hall);
        seat.setRowLabel(rowLabel);
        seat.setSeatLabel(seatLabel);
        Seat savedSeat = seatRepository.save(seat);

        return seatMapper.toSeatResponse(savedSeat);

    }

    @Override
    @Transactional
    public List<SeatResponse> getSeatsByHallUuid(UUID hallUuid) {

        hallRepository.findHallByUuid(hallUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Hall",
                                "uuid",
                                hallUuid
                        )
                );

        List<Seat> seats = seatRepository.findAllSeatByHallUuidOrderByRowLabelAscSeatNumberAsc(hallUuid);

        return seatMapper.toSeatResponseList(seats);
    }

    @Override
    @Transactional
    public SeatResponse getSeatByUuid(UUID uuid) {

        Seat seat = seatRepository.findSeatByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Seat",
                                "uuid",
                                uuid
                        )
                );

        return seatMapper.toSeatResponse(seat);
    }

    @Override
    @Transactional
    public SeatResponse updateSeatStatus(UUID uuid, UpdateSeatStatusRequest updateSeatStatusRequest) {

        Seat seat = seatRepository.findSeatByUuid(uuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Seat",
                                "uuid",
                                uuid
                        )
                );

        seat.setStatus(updateSeatStatusRequest.status());

        return seatMapper.toSeatResponse(seat);
    }
}
