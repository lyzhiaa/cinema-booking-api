package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.Ticket;
import kh.edu.istad.moviebooking.features.ticket.dto.TicketItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(
            source = "uuid",
            target = "ticketUuid"
    )
    @Mapping(
            source = "booking.showtime.movie.title",
            target = "movieTitle"
    )
    @Mapping(
            source = "booking.showtime.hall.name",
            target = "hallName"
    )
    @Mapping(
            source = "booking.showtime.startTime",
            target = "showDate",
            qualifiedByName = "toDate"
    )
    @Mapping(
            source = "booking.showtime.startTime",
            target = "showTime",
            qualifiedByName = "toTime"
    )
    @Mapping(
            source = "bookingSeat.seat.uuid",
            target = "seatUuid"
    )
    @Mapping(
            source = "bookingSeat.seat.seatLabel",
            target = "seatLabel"
    )
    @Mapping(
            source = "status",
            target = "status"
    )
    TicketItemResponse toTicketItemResponse(
            Ticket ticket
    );

    List<TicketItemResponse> toTicketItemResponseList(
            List<Ticket> tickets
    );

    @Named("toDate")
    default LocalDate toDate(
            LocalDateTime dateTime
    ) {
        return dateTime == null
                ? null
                : dateTime.toLocalDate();
    }

    @Named("toTime")
    default LocalTime toTime(
            LocalDateTime dateTime
    ) {
        return dateTime == null
                ? null
                : dateTime.toLocalTime();
    }
}