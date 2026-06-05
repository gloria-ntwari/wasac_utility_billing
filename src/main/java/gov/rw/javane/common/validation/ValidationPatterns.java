package gov.rw.javane.common.validation;

public final class ValidationPatterns {
    private ValidationPatterns() {}

    // Lowercase-only email (no capitals allowed)
    public static final String EMAIL_LOWERCASE =
            "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$";

    /** Rwanda local format — exactly 10 digits */
    public static final String PHONE_TEN_DIGITS = "^[0-9]{10}$";

    public static final String NATIONAL_ID_16 = "^[0-9]{16}$";

    public static final String STRONG_PASSWORD =
            "^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";
}

