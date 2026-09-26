package kh.edu.istad.moviebooking.features.groupBooking;

import kh.edu.istad.moviebooking.domain.Booking;
import kh.edu.istad.moviebooking.domain.GroupBooking;
import kh.edu.istad.moviebooking.domain.GroupMember;
import kh.edu.istad.moviebooking.domain.Showtime;
import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.domain.enums.BookingStatus;
import kh.edu.istad.moviebooking.domain.enums.GroupBookingStatus;
import kh.edu.istad.moviebooking.domain.enums.GroupMemberStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.auth.CurrentUserService;
import kh.edu.istad.moviebooking.features.booking.BookingRepository;
import kh.edu.istad.moviebooking.features.groupBooking.dto.CreateGroupBookingRequest;
import kh.edu.istad.moviebooking.features.groupBooking.dto.GroupBookingResponse;
import kh.edu.istad.moviebooking.features.groupBooking.dto.GroupMemberResponse;
import kh.edu.istad.moviebooking.features.showtime.ShowTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupBookingServiceImpl implements GroupBookingService {

    private final GroupBookingRepository groupBookingRepository;
    private final GroupMemberRepository groupMemberRepository;

    private final BookingRepository bookingRepository;
    private final ShowTimeRepository showtimeRepository;

    private final CurrentUserService currentUserService;

    private static final long GROUP_DURATION_MINUTES = 30;
    private static final long GROUP_PAYMENT_MINUTES = 10;

    @Override
    @Transactional
    public GroupBookingResponse createGroup(CreateGroupBookingRequest request) {

        User host = currentUserService.getCurrentUser();

        Showtime showtime = showtimeRepository.findShowtimeByUuid(request.showtimeUuid())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "Showtime",
                                        "uuid",
                                        request.showtimeUuid()
                                )
                        );

        String inviteToken = UUID.randomUUID()
                        .toString()
                        .replace("-", "");

        GroupBooking group = GroupBooking.builder()
                        .uuid(UUID.randomUUID())
                        .host(host)
                        .showtime(showtime)
                        .name(request.name())
                        .inviteToken(inviteToken)
                        .status(GroupBookingStatus.OPEN)
                        .expiresAt(LocalDateTime.now().plusMinutes(GROUP_DURATION_MINUTES))
                        .build();

        groupBookingRepository.save(group);

        GroupMember hostMember = GroupMember.builder()
                        .uuid(UUID.randomUUID())
                        .groupBooking(group)
                        .user(host)
                        .status(GroupMemberStatus.JOINED)
                        .joinedAt(LocalDateTime.now())
                        .build();

        groupMemberRepository.save(hostMember);

        return toResponse(group);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupBookingResponse getGroup(UUID groupUuid) {

        GroupBooking group = getGroupOrThrow(groupUuid);

        User currentUser = currentUserService.getCurrentUser();

        requireMember(groupUuid, currentUser.getUuid());

        return toResponse(group);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupBookingResponse getInvitation(String inviteToken) {

        GroupBooking group = groupBookingRepository
                        .findByInviteToken(inviteToken)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "GroupBooking",
                                        "inviteToken",
                                        inviteToken
                                )
                        );

        return toResponse(group);
    }

    @Override
    @Transactional
    public GroupBookingResponse joinGroup(
            String inviteToken
    ) {

        User currentUser =
                currentUserService.getCurrentUser();

        GroupBooking group =
                groupBookingRepository
                        .findByInviteTokenForUpdate(inviteToken)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "GroupBooking",
                                        "inviteToken",
                                        inviteToken
                                )
                        );

        if (group.getStatus()
                != GroupBookingStatus.OPEN) {

            throw new BadRequestException(
                    "Group booking is not open"
            );
        }

        if (group.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            group.setStatus(
                    GroupBookingStatus.EXPIRED
            );

            groupBookingRepository.save(group);

            throw new BadRequestException(
                    "Group booking has expired"
            );
        }

        GroupMember existingMember =
                groupMemberRepository
                        .findByGroupBookingUuidAndUserUuid(
                                group.getUuid(),
                                currentUser.getUuid()
                        )
                        .orElse(null);

        if (existingMember != null) {

            if (existingMember.getStatus()
                    != GroupMemberStatus.LEFT) {

                throw new BadRequestException(
                        "You already joined this group"
                );
            }

            existingMember.setStatus(
                    GroupMemberStatus.JOINED
            );

            existingMember.setJoinedAt(
                    LocalDateTime.now()
            );

            existingMember.setBooking(null);

            groupMemberRepository.save(
                    existingMember
            );

            return toResponse(group);
        }

        GroupMember member =
                GroupMember.builder()
                        .uuid(UUID.randomUUID())
                        .groupBooking(group)
                        .user(currentUser)
                        .status(
                                GroupMemberStatus.JOINED
                        )
                        .joinedAt(
                                LocalDateTime.now()
                        )
                        .build();

        groupMemberRepository.save(member);

        return toResponse(group);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getMembers(UUID groupUuid) {

        User currentUser = currentUserService.getCurrentUser();

        requireMember(groupUuid, currentUser.getUuid());

        return groupMemberRepository.findAllByGroupBookingUuidAndStatusNot(
                        groupUuid,
                        GroupMemberStatus.LEFT
                )
                .stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Override
    @Transactional
    public GroupBookingResponse attachBooking(UUID groupUuid, UUID bookingUuid) {

        User currentUser = currentUserService.getCurrentUser();

        GroupBooking group = groupBookingRepository
                        .findByUuidForUpdate(groupUuid)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "GroupBooking",
                                        "uuid",
                                        groupUuid
                                )
                        );

        validateGroupOpen(group);

        GroupMember member = requireMember(groupUuid, currentUser.getUuid());

        Booking booking = bookingRepository.findBookingByUuid(bookingUuid)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "Booking",
                                        "uuid",
                                        bookingUuid
                                )
                        );

        if (!booking.getUser()
                .getUuid()
                .equals(currentUser.getUuid())) {

            throw new BadRequestException("Booking does not belong to current user");
        }

        if (!booking.getShowtime()
                .getUuid()
                .equals(group.getShowtime().getUuid())) {

            throw new BadRequestException("Booking showtime does not match group showtime");
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {

            throw new BadRequestException("Booking is not waiting for payment"
            );
        }

        GroupMember anotherMember = groupMemberRepository
                        .findByBookingUuid(
                                bookingUuid
                        )
                        .orElse(null);

        if (anotherMember != null
                && !anotherMember.getUuid()
                .equals(member.getUuid())) {

            throw new BadRequestException(
                    "Booking already belongs to another group member"
            );
        }

        /*
         * Keep the member's booking valid for the
         * group lifetime instead of the normal
         * short payment deadline.
         */
        booking.setPaymentExpiresAt(
                group.getExpiresAt()
        );

        bookingRepository.save(booking);

        member.setBooking(booking);
        member.setStatus(
                GroupMemberStatus.SELECTING
        );

        groupMemberRepository.save(member);

        return toResponse(group);
    }

    @Override
    @Transactional
    public GroupBookingResponse markReady(UUID groupUuid) {

        User currentUser = currentUserService.getCurrentUser();

        GroupBooking group = getGroupOrThrow(groupUuid);

        validateGroupOpen(group);

        GroupMember member = requireMember(groupUuid, currentUser.getUuid());

        Booking booking = member.getBooking();

        if (booking == null) {
            throw new BadRequestException("Select and book your seats before marking ready");
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {

            throw new BadRequestException("Member booking is not waiting for payment");
        }

        member.setStatus(GroupMemberStatus.READY);

        groupMemberRepository.save(member);

        return toResponse(group);
    }

    @Override
    @Transactional
    public GroupBookingResponse markSelecting(UUID groupUuid) {

        User currentUser = currentUserService.getCurrentUser();

        GroupBooking group = getGroupOrThrow(groupUuid);

        validateGroupOpen(group);

        GroupMember member = requireMember(groupUuid, currentUser.getUuid());

        if (member.getBooking() == null) {
            throw new BadRequestException("Member does not have a booking");
        }

        member.setStatus(GroupMemberStatus.SELECTING);

        groupMemberRepository.save(member);

        return toResponse(group);
    }

    @Override
    @Transactional
    public GroupBookingResponse lockGroup(
            UUID groupUuid
    ) {

        User currentUser =
                currentUserService.getCurrentUser();

        GroupBooking group =
                groupBookingRepository
                        .findByUuidForUpdate(groupUuid)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "GroupBooking",
                                        "uuid",
                                        groupUuid
                                )
                        );

        // 1. Group must still be OPEN
        validateGroupOpen(group);

        // 2. Only host can lock
        requireHost(group, currentUser);

        // 3. Get active members
        List<GroupMember> members =
                groupMemberRepository
                        .findAllByGroupBookingUuidAndStatusNot(
                                groupUuid,
                                GroupMemberStatus.LEFT
                        );

        if (members.isEmpty()) {
            throw new BadRequestException(
                    "Group has no members"
            );
        }

        // 4. FIND HOST MEMBER HERE
        GroupMember hostMember =
                members.stream()
                        .filter(member ->
                                member.getUser()
                                        .getUuid()
                                        .equals(
                                                group.getHost()
                                                        .getUuid()
                                        )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Group host is not a member of the group"
                                )
                        );

        // 5. Host must have selected seat / created booking
        if (hostMember.getBooking() == null) {
            throw new BadRequestException(
                    "Host must select seats before locking the group"
            );
        }

        // 6. All OTHER members must be READY
        boolean allMembersReady =
                members.stream()
                        .filter(member ->
                                !member.getUser()
                                        .getUuid()
                                        .equals(
                                                group.getHost()
                                                        .getUuid()
                                        )
                        )
                        .allMatch(member ->
                                member.getStatus()
                                        == GroupMemberStatus.READY
                                        &&
                                        member.getBooking() != null
                        );

        if (!allMembersReady) {
            throw new BadRequestException(
                    "All group members must be ready before locking"
            );
        }

        // 7. Create common payment deadline
        LocalDateTime paymentDeadline =
                LocalDateTime.now()
                        .plusMinutes(
                                GROUP_PAYMENT_MINUTES
                        );

        // 8. Validate all bookings
        for (GroupMember member : members) {

            Booking booking =
                    member.getBooking();

            if (booking == null) {
                throw new BadRequestException(
                        "All members must have a booking"
                );
            }

            if (booking.getStatus()
                    != BookingStatus.PENDING_PAYMENT) {

                throw new BadRequestException(
                        "All member bookings must be waiting for payment"
                );
            }

            booking.setPaymentExpiresAt(
                    paymentDeadline
            );
        }

        bookingRepository.saveAll(
                members.stream()
                        .map(GroupMember::getBooking)
                        .toList()
        );

        // Optional:
        // Host pressing LOCK means host is also ready
        hostMember.setStatus(
                GroupMemberStatus.READY
        );

        groupMemberRepository.save(hostMember);

        // 9. Lock group
        group.setStatus(
                GroupBookingStatus.LOCKED
        );

        group.setLockedAt(
                LocalDateTime.now()
        );

        group.setPaymentExpiresAt(
                paymentDeadline
        );

        groupBookingRepository.save(group);

        return toResponse(group);
    }

    private GroupBooking getGroupOrThrow(UUID groupUuid) {

        return groupBookingRepository.findByUuid(groupUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "GroupBooking",
                                "uuid",
                                groupUuid
                        )
                );
    }

    private GroupMember requireMember(UUID groupUuid, UUID userUuid) {

        GroupMember member = groupMemberRepository.findByGroupBookingUuidAndUserUuid(
                                groupUuid,
                                userUuid
                        )
                        .orElseThrow(() -> new BadRequestException("You are not a member of this group"));

        if (member.getStatus() == GroupMemberStatus.LEFT) {

            throw new BadRequestException("You are not an active member of this group");
        }

        return member;
    }

    private void validateGroupOpen(GroupBooking group) {

        if (group.getStatus() != GroupBookingStatus.OPEN) {

            throw new BadRequestException("Group booking is not open"
            );
        }

        if (group.getExpiresAt().isBefore(LocalDateTime.now())) {

            throw new BadRequestException("Group booking has expired");
        }
    }

    private void requireHost(GroupBooking group, User currentUser) {

        if (!group.getHost()
                .getUuid()
                .equals(currentUser.getUuid())) {

            throw new BadRequestException("Only the group host can perform this action");
        }
    }

    private GroupBookingResponse toResponse(GroupBooking group) {

        long memberCount = groupMemberRepository.countByGroupBookingUuidAndStatusNot(
                                group.getUuid(),
                                GroupMemberStatus.LEFT
                        );

        long readyCount = groupMemberRepository.countByGroupBookingUuidAndStatus(
                                group.getUuid(),
                                GroupMemberStatus.READY
                        );

        return new GroupBookingResponse(
                group.getUuid(),
                group.getName(),
                group.getInviteToken(),
                group.getStatus(),
                group.getHost().getUuid(),
                group.getShowtime().getUuid(),
                memberCount,
                readyCount,
                group.getExpiresAt(),
                group.getLockedAt(),
                group.getPaymentExpiresAt()
        );
    }

    private GroupMemberResponse toMemberResponse(GroupMember member) {

        UUID bookingUuid = null;

        if (member.getBooking() != null) {
            bookingUuid = member.getBooking()
                            .getUuid();
        }

        return new GroupMemberResponse(
                member.getUuid(),
                member.getUser().getUuid(),
                member.getUser().getFirstName(),
                member.getUser().getLastName(),
                bookingUuid,
                member.getStatus(),
                member.getJoinedAt()
        );
    }
}