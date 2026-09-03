package kh.edu.istad.moviebooking.features.hall;

import kh.edu.istad.moviebooking.domain.Hall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HallRepository extends JpaRepository<Hall, Long> {
//    find hall by uuid
    Optional<Hall> findHallByUuid(UUID uuid);
//
    boolean existsByNameIgnoreCase(String name);
}
