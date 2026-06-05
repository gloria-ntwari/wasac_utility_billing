package gov.rw.javane.repository;

import gov.rw.javane.domain.entity.AppUser;
import gov.rw.javane.domain.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    long countByStatus(UserStatus status);
}
