package gov.rw.javane.service;

import gov.rw.javane.domain.enums.RoleName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:}")
    private String fromAddress;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public boolean sendStaffCredentialsEmail(String toEmail, String fullNames, RoleName role,
                                             String temporaryPassword, String otpCode, int otpExpirationMinutes) {
        String from = resolveFromAddress();
        if (from == null || from.isBlank()) {
            log.warn("Email not sent to {} — SMTP/from address is not configured", toEmail);
            return false;
        }

        String roleLabel = role.name().replace("ROLE_", "").toLowerCase();
        String body = """
                Dear %s,

                Your Utility Billing System account has been created by an administrator.

                Account details:
                - Username / Email: %s
                - Temporary password: %s
                - Assigned role: %s

                Email verification OTP: %s
                (Valid for %d minutes)

                Getting started:
                1. Verify your email: POST /auth/verify-otp with your email and the OTP above.
                2. Login: POST /auth/login using your email and temporary password.
                3. Use Swagger UI at /swagger-ui.html or your API client with the JWT token returned.

                If you did not expect this account, contact your system administrator immediately.

                Regards,
                WASAC / REG Utility Billing System
                """.formatted(fullNames, toEmail, temporaryPassword, roleLabel, otpCode, otpExpirationMinutes);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("Your Utility Billing System account credentials");
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("Staff credentials email sent to {}", toEmail);
            return true;
        } catch (MailException ex) {
            log.error("Failed to send credentials email to {}: {}", toEmail, ex.getMessage());
            return false;
        }
    }

    public boolean sendCustomerNotificationEmail(String toEmail, String fullName, String message, String subject) {
        String from = resolveFromAddress();
        if (from == null || from.isBlank()) {
            log.warn("Bill notification email not sent to {} — SMTP/from address is not configured", toEmail);
            return false;
        }

        String body = """
                %s

                You can view your bills and payment history in the Utility Billing System.

                Regards,
                WASAC / REG Utility Billing System
                """.formatted(message);

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(toEmail);
        mail.setSubject(subject);
        mail.setText(body);

        try {
            mailSender.send(mail);
            log.info("Customer notification email sent to {}", toEmail);
            return true;
        } catch (MailException ex) {
            log.error("Failed to send notification email to {}: {}", toEmail, ex.getMessage());
            return false;
        }
    }

    private String resolveFromAddress() {
        if (fromAddress != null && !fromAddress.isBlank()) {
            return fromAddress;
        }
        return mailUsername;
    }
}
