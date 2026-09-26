package kh.edu.istad.moviebooking.features.groupBooking;

import jakarta.validation.Valid;
import kh.edu.istad.moviebooking.features.groupBooking.dto.CreateGroupBookingRequest;
import kh.edu.istad.moviebooking.features.groupBooking.dto.GroupBookingResponse;
import kh.edu.istad.moviebooking.features.groupBooking.dto.GroupMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/group-bookings")
@RequiredArgsConstructor
public class GroupBookingController {

    private final GroupBookingService
            groupBookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupBookingResponse createGroup(
            @Valid
            @RequestBody
            CreateGroupBookingRequest request
    ) {

        return groupBookingService
                .createGroup(request);
    }

    @GetMapping("/{groupUuid}")
    public GroupBookingResponse getGroup(@PathVariable UUID groupUuid) {

        return groupBookingService
                .getGroup(groupUuid);
    }

    @GetMapping(
            "/invitations/{inviteToken}"
    )
    public GroupBookingResponse getInvitation(
            @PathVariable String inviteToken
    ) {

        return groupBookingService
                .getInvitation(inviteToken);
    }

    @PostMapping(
            "/join/{inviteToken}"
    )
    public GroupBookingResponse joinGroup(
            @PathVariable String inviteToken
    ) {

        return groupBookingService
                .joinGroup(inviteToken);
    }

    @GetMapping(
            "/{groupUuid}/members"
    )
    public List<GroupMemberResponse> getMembers(
            @PathVariable UUID groupUuid
    ) {

        return groupBookingService
                .getMembers(groupUuid);
    }

    @PutMapping(
            "/{groupUuid}/members/me/booking/{bookingUuid}"
    )
    public GroupBookingResponse attachBooking(
            @PathVariable UUID groupUuid,
            @PathVariable UUID bookingUuid
    ) {

        return groupBookingService
                .attachBooking(
                        groupUuid,
                        bookingUuid
                );
    }

    @PatchMapping(
            "/{groupUuid}/members/me/ready"
    )
    public GroupBookingResponse markReady(
            @PathVariable UUID groupUuid
    ) {

        return groupBookingService
                .markReady(groupUuid);
    }

    @PatchMapping(
            "/{groupUuid}/members/me/selecting"
    )
    public GroupBookingResponse markSelecting(
            @PathVariable UUID groupUuid
    ) {

        return groupBookingService
                .markSelecting(groupUuid);
    }

    @PostMapping(
            "/{groupUuid}/lock"
    )
    public GroupBookingResponse lockGroup(
            @PathVariable UUID groupUuid
    ) {

        return groupBookingService
                .lockGroup(groupUuid);
    }
}