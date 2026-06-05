package gov.rw.javane.service;

import gov.rw.javane.common.exception.BadRequestException;
import gov.rw.javane.common.exception.ForbiddenException;
import gov.rw.javane.common.exception.NotFoundException;
import gov.rw.javane.common.validation.FieldCrossValidator;
import gov.rw.javane.domain.entity.AppUser;
import gov.rw.javane.domain.entity.Role;
import gov.rw.javane.domain.enums.RoleName;
import gov.rw.javane.domain.enums.UserStatus;
import gov.rw.javane.dto.user.AdminUserUpdateRequest;
import gov.rw.javane.dto.user.ProfileUpdateRequest;
import gov.rw.javane.dto.user.UserCreateRequest;
import gov.rw.javane.dto.user.UserCreateResponse;
import gov.rw.javane.dto.user.UserResponse;
import gov.rw.javane.mapper.EntityMapper;
import gov.rw.javane.repository.AppUserRepository;
import gov.rw.javane.repository.CustomerRepository;
import gov.rw.javane.repository.RoleRepository;
import gov.rw.javane.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;
    private final OtpService otpService;
    private final EmailService emailService;
    private final SecurityUtils securityUtils;

    @Value("${app.otp.expiration-minutes:10}")
    private int otpExpirationMinutes;

    @Transactional
    public UserCreateResponse create(UserCreateRequest request) {
        if (request.role() == RoleName.ROLE_CUSTOMER) {
            throw new BadRequestException("Customer accounts must register via /auth/signup");
        }
        if (request.role() == RoleName.ROLE_ADMIN) {
            throw new BadRequestException("Admin accounts cannot be created through this endpoint");
        }

        String email = request.email().toLowerCase().trim();
        String temporaryPassword = temporaryPasswordGenerator.generate();

        FieldCrossValidator.rejectEmailImpersonation(email, request.fullNames(), request.phoneNumber(), temporaryPassword);

        if (appUserRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered: " + email);
        }
        if (appUserRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new BadRequestException("Phone number is already registered");
        }

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new BadRequestException("Role not found: " + request.role()));

        String otpCode = otpService.generateOtp();
        Instant otpExpiresAt = Instant.now().plus(otpExpirationMinutes, ChronoUnit.MINUTES);

        AppUser user = AppUser.builder()
                .fullNames(request.fullNames().trim())
                .email(email)
                .phoneNumber(request.phoneNumber().trim())
                .password(passwordEncoder.encode(temporaryPassword))
                .status(request.status() != null ? request.status() : UserStatus.ACTIVE)
                .emailVerified(false)
                .otpCode(otpCode)
                .otpExpiresAt(otpExpiresAt)
                .roles(Set.of(role))
                .build();

        user = appUserRepository.save(user);

        boolean emailSent = emailService.sendStaffCredentialsEmail(
                email,
                user.getFullNames(),
                role.getName(),
                temporaryPassword,
                otpCode,
                otpExpirationMinutes
        );

        UserResponse base = EntityMapper.toUserResponse(user);
        String message = emailSent
                ? "User account created. Temporary password and OTP have been sent to " + email
                : "User account created. Credentials email could not be sent — configure SMTP";

        return new UserCreateResponse(
                base.id(),
                base.fullNames(),
                base.email(),
                base.phoneNumber(),
                base.status(),
                base.emailVerified(),
                base.roles(),
                base.createdAt(),
                emailSent,
                message
        );
    }

    public UserResponse findMe() {
        return EntityMapper.toUserResponse(securityUtils.currentUser());
    }

    public List<UserResponse> findAll() {
        return appUserRepository.findAll().stream().map(EntityMapper::toUserResponse).toList();
    }

    public UserResponse findById(UUID id) {
        AppUser current = securityUtils.currentUser();
        if (!isAdmin(current) && !current.getId().equals(id)) {
            throw new ForbiddenException("You can only view your own profile");
        }
        return EntityMapper.toUserResponse(getUser(id));
    }

    @Transactional
    public UserResponse updateMyProfile(ProfileUpdateRequest request) {
        AppUser user = securityUtils.currentUser();
        applyProfileFields(user, request.fullNames(), request.email(), request.phoneNumber());
        return EntityMapper.toUserResponse(appUserRepository.save(user));
    }

    @Transactional
    public UserResponse updateByAdmin(UUID id, AdminUserUpdateRequest request) {
        AppUser user = getUser(id);
        if (request.role() == RoleName.ROLE_CUSTOMER) {
            throw new BadRequestException("Use /auth/signup to manage customer accounts");
        }

        applyProfileFields(user, request.fullNames(), request.email(), request.phoneNumber());

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new BadRequestException("Role not found: " + request.role()));
        user.setStatus(request.status() != null ? request.status() : user.getStatus());
        user.setRoles(Set.of(role));

        return EntityMapper.toUserResponse(appUserRepository.save(user));
    }

    @Transactional
    public void delete(UUID id) {
        AppUser user = getUser(id);

        if (securityUtils.currentUser().getId().equals(id)) {
            throw new BadRequestException("You cannot delete your own account while logged in");
        }

        if (user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN)) {
            long adminCount = appUserRepository.findAll().stream()
                    .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN))
                    .count();
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot delete the last admin account");
            }
        }

        detachLinkedCustomer(user);
        appUserRepository.delete(user);
    }

    private void detachLinkedCustomer(AppUser user) {
        customerRepository.findByUserId(user.getId()).ifPresent(customer -> {
            customer.setUser(null);
            customerRepository.saveAndFlush(customer);
        });
        user.setCustomer(null);
    }

    private void applyProfileFields(AppUser user, String fullNames, String email, String phoneNumber) {
        String normalizedEmail = email.toLowerCase().trim();
        if (!user.getEmail().equals(normalizedEmail) && appUserRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email is already registered: " + normalizedEmail);
        }
        if (!user.getPhoneNumber().equals(phoneNumber.trim()) && appUserRepository.existsByPhoneNumber(phoneNumber.trim())) {
            throw new BadRequestException("Phone number is already registered");
        }
        FieldCrossValidator.rejectEmailImpersonation(normalizedEmail, fullNames, phoneNumber, null);

        user.setFullNames(fullNames.trim());
        user.setEmail(normalizedEmail);
        user.setPhoneNumber(phoneNumber.trim());
    }

    private boolean isAdmin(AppUser user) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN);
    }

    private AppUser getUser(UUID id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }
}
