package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.features.auth.dto.RegisterRequest;
import kh.edu.istad.moviebooking.features.user.dto.CreateUserRequest;
import kh.edu.istad.moviebooking.features.user.dto.UpdateUserRequest;
import kh.edu.istad.moviebooking.features.user.dto.UserResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Create Request -> User Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "points", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "disabled", ignore = true)
    @Mapping(target = "role", ignore = true)
    User fromUserCreateRequest(CreateUserRequest createUserRequest);


    // User Entity -> Response
    @Mapping(source = "role.name", target = "role")
    UserResponse toUserResponse(User user);


    // List<User> -> List<UserResponse>
    List<UserResponse> toUserResponseList(List<User> users);


    // Update existing User
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "points", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "disabled", ignore = true)
    void fromUserUpdateRequest(UpdateUserRequest updateUserRequest, @MappingTarget User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "points", ignore = true)
    @Mapping(target = "disabled", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User fromRegisterRequest(RegisterRequest registerRequest);
}