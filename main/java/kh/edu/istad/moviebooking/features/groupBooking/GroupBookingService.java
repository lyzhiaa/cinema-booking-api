package kh.edu.istad.moviebooking.features.groupBooking;

import kh.edu.istad.moviebooking.features.groupBooking.dto.GroupBookingResponse;
import kh.edu.istad.moviebooking.features.groupBooking.dto.GroupMemberResponse;
import kh.edu.istad.moviebooking.features.groupBooking.dto.CreateGroupBookingRequest;

import java.util.List;
import java.util.UUID;

public interface GroupBookingService {

    GroupBookingResponse createGroup(CreateGroupBookingRequest request);

    GroupBookingResponse getGroup(UUID groupUuid);

    GroupBookingResponse getInvitation(String inviteToken);

    GroupBookingResponse joinGroup(String inviteToken);

    List<GroupMemberResponse> getMembers(UUID groupUuid);

    GroupBookingResponse attachBooking(UUID groupUuid, UUID bookingUuid);

    GroupBookingResponse markReady(UUID groupUuid);

    GroupBookingResponse markSelecting(UUID groupUuid);

    GroupBookingResponse lockGroup(UUID groupUuid);
}