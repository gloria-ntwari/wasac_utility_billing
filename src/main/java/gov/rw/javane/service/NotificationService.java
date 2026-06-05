package gov.rw.javane.service;

import gov.rw.javane.common.exception.ForbiddenException;
import gov.rw.javane.common.exception.NotFoundException;
import gov.rw.javane.domain.entity.AppUser;
import gov.rw.javane.domain.entity.Customer;
import gov.rw.javane.domain.entity.Notification;
import gov.rw.javane.domain.enums.RoleName;
import gov.rw.javane.dto.notification.NotificationResponse;
import gov.rw.javane.mapper.EntityMapper;
import gov.rw.javane.repository.CustomerRepository;
import gov.rw.javane.repository.NotificationRepository;
import gov.rw.javane.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;
    private final SecurityUtils securityUtils;

    public List<NotificationResponse> findAll(UUID customerId) {
        if (customerId != null) {
            return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                    .stream().map(EntityMapper::toNotificationResponse).toList();
        }
        AppUser user = securityUtils.currentUser();
        if (hasRole(user, RoleName.ROLE_CUSTOMER)) {
            Customer customer = customerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Customer profile not found"));
            return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId())
                    .stream().map(EntityMapper::toNotificationResponse).toList();
        }
        return notificationRepository.findAll().stream()
                .map(EntityMapper::toNotificationResponse).toList();
    }

    public NotificationResponse findById(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found with id: " + id));
        assertCustomerAccess(notification);
        return EntityMapper.toNotificationResponse(notification);
    }

    @Transactional
    public void delete(UUID id) {
        notificationRepository.delete(notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found with id: " + id)));
    }

    private void assertCustomerAccess(Notification notification) {
        AppUser user = securityUtils.currentUser();
        if (hasRole(user, RoleName.ROLE_CUSTOMER)) {
            Customer customer = customerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Customer profile not found"));
            if (!notification.getCustomer().getId().equals(customer.getId())) {
                throw new ForbiddenException("You can only view your own notifications");
            }
        }
    }

    private boolean hasRole(AppUser user, RoleName roleName) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == roleName);
    }
}
