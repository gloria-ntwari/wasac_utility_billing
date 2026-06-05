package gov.rw.javane.service;

import gov.rw.javane.common.exception.BadRequestException;
import gov.rw.javane.common.exception.UnauthorizedException;
import gov.rw.javane.domain.entity.AppUser;
import gov.rw.javane.domain.entity.Customer;
import gov.rw.javane.domain.entity.Role;
import gov.rw.javane.domain.enums.CustomerStatus;
import gov.rw.javane.domain.enums.RoleName;
import gov.rw.javane.domain.enums.UserStatus;
import gov.rw.javane.dto.auth.AuthResponse;
import gov.rw.javane.dto.auth.LoginRequest;
import gov.rw.javane.dto.auth.SignupRequest;
import gov.rw.javane.dto.auth.VerifyOtpRequest;
import gov.rw.javane.common.validation.FieldCrossValidator;
import gov.rw.javane.repository.AppUserRepository;
import gov.rw.javane.repository.CustomerRepository;
import gov.rw.javane.repository.RoleRepository;
import gov.rw.javane.security.JwtService;
import gov.rw.javane.security.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsServiceBridge userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String email = request.email().toLowerCase().trim();
        String nationalId = request.nationalId().trim();

        FieldCrossValidator.rejectEmailImpersonation(email, request.fullNames(), request.phoneNumber(), request.password());

        if (appUserRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered: " + email);
        }
        if (appUserRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new BadRequestException("Phone number is already registered");
        }
        if (customerRepository.existsByNationalId(nationalId)) {
            throw new BadRequestException("Customer with this National ID already exists");
        }
        if (customerRepository.existsByEmail(email)) {
            throw new BadRequestException("Customer email is already registered");
        }

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new BadRequestException("Customer role is not configured"));

        AppUser user = AppUser.builder()
                .fullNames(request.fullNames().trim())
                .email(email)
                .phoneNumber(request.phoneNumber().trim())
                .password(passwordEncoder.encode(request.password()))
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .roles(Set.of(customerRole))
                .build();
        appUserRepository.save(user);

        Customer customer = Customer.builder()
                .fullName(request.fullNames().trim())
                .nationalId(nationalId)
                .email(email)
                .phoneNumber(request.phoneNumber().trim())
                .address(request.address().trim())
                .status(CustomerStatus.ACTIVE)
                .user(user)
                .build();
        customerRepository.save(customer);
        user.setCustomer(customer);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                "Customer account registered successfully",
                user.getId(),
                customer.getId(),
                user.getEmail(),
                user.getFullNames(),
                Set.of(RoleName.ROLE_CUSTOMER)
        );
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase().trim();
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Your account is inactive. Contact the administrator.");
        }

        boolean isStaff = user.getRoles().stream()
                .anyMatch(r -> r.getName() != RoleName.ROLE_CUSTOMER);
        if (isStaff && !user.isEmailVerified()) {
            throw new UnauthorizedException("Email not verified. Check your inbox for the OTP and call POST /auth/verify-otp before logging in.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(userDetails);

        Set<RoleName> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        UUID customerId = customerRepository.findByUserId(user.getId())
                .map(Customer::getId)
                .orElse(null);
        return new AuthResponse(
                token,
                "Login successful",
                user.getId(),
                customerId,
                user.getEmail(),
                user.getFullNames(),
                roles
        );
    }

    @Transactional
    public void verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("No account found with email: " + email));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified. You can login now.");
        }
        if (user.getOtpCode() == null || user.getOtpExpiresAt() == null) {
            throw new BadRequestException("No active OTP for this account. Contact your administrator.");
        }
        if (Instant.now().isAfter(user.getOtpExpiresAt())) {
            throw new BadRequestException("OTP has expired. Contact your administrator to resend credentials.");
        }
        if (!user.getOtpCode().equals(request.getOtp().trim())) {
            throw new BadRequestException("Invalid OTP. Please check the code sent to your email.");
        }

        user.setEmailVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        appUserRepository.save(user);
    }

    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Authorization header with Bearer token is required for logout");
        }
        String token = authHeader.substring(7);
        tokenBlacklistService.revoke(token, jwtService.extractExpiration(token));
    }
}
