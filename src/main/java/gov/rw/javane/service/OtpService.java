package gov.rw.javane.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
