package kh.edu.istad.moviebooking.features.user;

import kh.edu.istad.moviebooking.domain.Role;
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

    Optional<User> findUserByUsername(String username);

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByUsernameOrEmail(String username, String email);

    Optional<Role> findUserByRoleName(String name);

    Optional<User> findByUsernameOrEmail(String username, String email);
}
