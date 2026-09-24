package kh.edu.istad.moviebooking.features.concession;



import kh.edu.istad.moviebooking.domain.ConcessionOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConcessionOrderItemRepository extends JpaRepository<ConcessionOrderItem, Long> {

    boolean existsByConcessionItemUuid(UUID concessionUuid);
}
