package kh.edu.istad.moviebooking.features.groupBooking;

import kh.edu.istad.moviebooking.domain.GroupMember;
import kh.edu.istad.moviebooking.domain.enums.GroupBookingStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupBookingGuardService {

    private final GroupMemberRepository groupMemberRepository;

    public void assertBookingEditable(UUID bookingUuid) {

        GroupMember member = groupMemberRepository.findByBookingUuid(bookingUuid)
                        .orElse(null);

        if (member == null) {
            return;
        }

        GroupBookingStatus status = member.getGroupBooking().getStatus();

        if (status != GroupBookingStatus.OPEN) {

            throw new BadRequestException("Group booking is locked and can no longer be edited"
            );
        }
    }
}