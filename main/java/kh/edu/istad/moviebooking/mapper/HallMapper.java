package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.Hall;
import kh.edu.istad.moviebooking.features.hall.dto.CreateHallRequest;
import kh.edu.istad.moviebooking.features.hall.dto.HallResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HallMapper {
//    create hall
    Hall fromCreateHallRequest(CreateHallRequest createHallRequest);
//    get all halls
    List<HallResponse> toHallResponseList(List<Hall> halls);
//    get hall by uuid
    HallResponse toHallResponse(Hall hall);

}
