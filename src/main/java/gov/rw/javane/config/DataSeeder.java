package gov.rw.javane.config;

import gov.rw.javane.domain.entity.AppUser;
import gov.rw.javane.domain.entity.Role;
import gov.rw.javane.domain.enums.RoleName;
import gov.rw.javane.domain.enums.UserStatus;
import gov.rw.javane.repository.AppUserRepository;
import gov.rw.javane.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email:admin@wasac.rw}")
    private String adminEmail;

    @Value("${app.seed.admin-password:Admin@12345}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() ->
                    roleRepository.save(Role.builder().name(roleName).build()));
        }

        seedStaffUser(adminEmail, adminPassword, "System Administrator", "0788000001", RoleName.ROLE_ADMIN);
        seedStaffUser("admin01@gmail.com", "admin01@123", "Test Admin", "0788000010", RoleName.ROLE_ADMIN);
        seedStaffUser("finance@gmail.com", "finance@123", "Test Finance Officer", "0788000011", RoleName.ROLE_FINANCE);
        seedStaffUser("operator@gmail.com", "operator@123", "Test Operator", "0788000012", RoleName.ROLE_OPERATOR);
        grandfatherExistingAccounts();
    }

    /**
     * Existing accounts created before customer OTP signup never received an otpCode.
     * Keep them verified; new signups always have otpCode set and must verify.
     */
    private void grandfatherExistingAccounts() {
        int count = 0;
        for (AppUser user : appUserRepository.findAll()) {
            if (!user.isEmailVerified() && user.getOtpCode() == null) {
                user.setEmailVerified(true);
                user.setOtpExpiresAt(null);
                appUserRepository.save(user);
                count++;
            }
        }
        if (count > 0) {
            log.info("Grandfathered {} legacy account(s) as OTP-verified", count);
        }
    }

    private void seedStaffUser(String email, String rawPassword, String fullNames, String phone, RoleName roleName) {
        String normalizedEmail = email.toLowerCase().trim();
        if (appUserRepository.findByEmail(normalizedEmail).isPresent()) {
            appUserRepository.findByEmail(normalizedEmail).ifPresent(existing -> {
                if (!existing.isEmailVerified()) {
                    existing.setEmailVerified(true);
                    existing.setOtpCode(null);
                    existing.setOtpExpiresAt(null);
                    appUserRepository.save(existing);
                    log.info("Marked seeded user as OTP verified: {}", normalizedEmail);
                }
            });
            return;
        }

        Role role = roleRepository.findByName(roleName).orElseThrow();
        AppUser user = AppUser.builder()
                .fullNames(fullNames)
                .email(normalizedEmail)
                .phoneNumber(phone)
                .password(passwordEncoder.encode(rawPassword))
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .otpCode(null)
                .otpExpiresAt(null)
                .roles(Set.of(role))
                .build();
        appUserRepository.save(user);
        log.info("Seeded test user (OTP pre-verified): {} [{}]", normalizedEmail, roleName);
    }
}
