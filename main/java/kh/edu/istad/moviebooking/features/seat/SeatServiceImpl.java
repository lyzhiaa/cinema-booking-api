package kh.edu.istad.moviebooking.features.seat;

import jakarta.transaction.Transactional;
import kh.edu.istad.moviebooking.domain.Hall;
import kh.edu.istad.moviebooking.domain.Seat;
import kh.edu.istad.moviebooking.domain.enums.SeatStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceAlreadyExistsException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.hall.HallRepository;
import kh.edu.istad.moviebooking.features.seat.dto.*;
import kh.edu.istad.moviebooking.mapper.SeatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        // 2. ADD CAPACITY CHECK HERE
        long currentSeats = seatRepository.countByHallUuid(hallUuid);

        if (currentSeats >= hall.getCapacity()) {
            throw new BadRequestException("Hall has reached maximum capacity of "
                            + hall.getCapacity()
                            + " seats"
            );
        }
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

//    create multiple seats
    @Override
    public List<SeatResponse> createSeatsBulk(UUID hallUuid, BulkCreateSeatRequest bulkCreateSeatRequest) {
        Hall hall = hallRepository.findHallByUuid(hallUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Hall", "uuid", hallUuid));

        int requestedSeats = bulkCreateSeatRequest.rows()
                .stream()
                .mapToInt(SeatRowRequest::numberOfSeats)
                .sum();

        long currentSeats = seatRepository.countByHallUuid(hallUuid);

        if (currentSeats + requestedSeats > hall.getCapacity()) {
            throw new BadRequestException(
                    "Cannot create " + requestedSeats
                            + " seats. Hall capacity is "
                            + hall.getCapacity()
                            + " and currently has "
                            + currentSeats
                            + " seats."
            );
        }

        List<Seat> seats = new ArrayList<>();

        for (SeatRowRequest row : bulkCreateSeatRequest.rows()) {

            String rowLabel = row.rowLabel()
                    .trim()
                    .toUpperCase();

            for (int number = 1;
                 number <= row.numberOfSeats();
                 number++) {

                String seatLabel =
                        rowLabel + number;

                if (seatRepository.existsByHallUuidAndSeatLabel(hallUuid, seatLabel)) {
                    throw new ResourceAlreadyExistsException(
                            "Seat",
                            "seatLabel",
                            seatLabel
                    );
                }

                Seat seat = new Seat();

                seat.setHall(hall);
                seat.setRowLabel(rowLabel);
                seat.setSeatNumber(number);
                seat.setSeatLabel(seatLabel);
                seat.setSeatType(row.seatType());
                seat.setStatus(SeatStatus.ACTIVE);

                seats.add(seat);
            }
        }

        List<Seat> savedSeats = seatRepository.saveAll(seats);

        return seatMapper.toSeatResponseList(savedSeats);
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
