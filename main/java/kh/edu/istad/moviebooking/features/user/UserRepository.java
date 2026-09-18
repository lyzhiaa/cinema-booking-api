package kh.edu.istad.moviebooking.features.user;

import kh.edu.istad.moviebooking.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByUuid(UUID uuid);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<User> findAllByIsDeletedFalse();
}
