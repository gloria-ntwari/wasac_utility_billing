package gov.rw.javane.security;

import gov.rw.javane.common.exception.UnauthorizedException;
import gov.rw.javane.domain.entity.AppUser;
import gov.rw.javane.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final AppUserRepository appUserRepository;

    public AppUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("Authentication required");
        }
        return appUserRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
    }

    public String currentEmail() {
        return currentUser().getEmail();
    }
}
