package gov.rw.javane.common.validation;

import gov.rw.javane.common.exception.BadRequestException;

public final class FieldCrossValidator {

    private FieldCrossValidator() {}

    public static void rejectEmailImpersonation(String email, String fullNames, String phoneNumber, String password) {
        if (email.equalsIgnoreCase(fullNames.trim())) {
            throw new BadRequestException("Email cannot be the same as full names");
        }
        if (email.equalsIgnoreCase(phoneNumber.trim())) {
            throw new BadRequestException("Email cannot be the same as phone number");
        }
        if (password != null && email.equalsIgnoreCase(password)) {
            throw new BadRequestException("Email cannot be the same as password");
        }
        if (email.contains("@") == false) {
            throw new BadRequestException("Invalid email format");
        }
        if (email.chars().anyMatch(Character::isUpperCase)) {
            throw new BadRequestException("Email must not contain capital letters");
        }
    }
}
