package kh.edu.istad.moviebooking.features.groupBooking;

import kh.edu.istad.moviebooking.domain.GroupMember;
import kh.edu.istad.moviebooking.domain.enums.GroupMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository
        extends JpaRepository<GroupMember, Long> {

    Optional<GroupMember>
    findByGroupBookingUuidAndUserUuid(UUID groupUuid, UUID userUuid);

    Optional<GroupMember>
    findByBookingUuid(UUID bookingUuid);

    boolean existsByBookingUuid(UUID bookingUuid);

    List<GroupMember> findAllByGroupBookingUuidAndStatusNot(UUID groupUuid, GroupMemberStatus status);

    long countByGroupBookingUuidAndStatusNot(UUID groupUuid, GroupMemberStatus status);

    long countByGroupBookingUuidAndStatus(UUID groupUuid, GroupMemberStatus status);
}