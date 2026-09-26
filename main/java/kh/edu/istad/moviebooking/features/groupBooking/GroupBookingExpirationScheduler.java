package kh.edu.istad.moviebooking.features.groupBooking;

import kh.edu.istad.moviebooking.domain.GroupBooking;
import kh.edu.istad.moviebooking.domain.enums.GroupBookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupBookingExpirationScheduler {

    private final GroupBookingRepository groupBookingRepository;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void expireGroups() {

        List<GroupBooking> groups = groupBookingRepository
                        .findAllByStatusAndExpiresAtBefore(
                                GroupBookingStatus.OPEN,
                                LocalDateTime.now()
                        );

        for (GroupBooking group : groups) {

            group.setStatus(GroupBookingStatus.EXPIRED);
        }

        groupBookingRepository.saveAll(groups);
    }
}