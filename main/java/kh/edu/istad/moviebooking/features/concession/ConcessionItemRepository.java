package kh.edu.istad.moviebooking.features.concession;

import kh.edu.istad.moviebooking.domain.ConcessionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConcessionItemRepository extends JpaRepository<ConcessionItem, Long> {

    List<ConcessionItem> findAllByActiveTrueOrderByCreatedAtDesc();

    Optional<ConcessionItem> findByUuidAndActiveTrue(UUID uuid);

    Optional<ConcessionItem> findByUuid(UUID uuid);

    boolean existsByNameIgnoreCase(String name);

    Optional<ConcessionItem> findByNameIgnoreCase(String name);


}