package com.wornux.constants;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ValidationConstants {
    // User validation constants
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    // Client validation constants
    public static final int MAX_IDENTIFICATION_DOCUMENT_COUNT = 1;
    public static final int MIN_IDENTIFICATION_DOCUMENT_COUNT = 1;

    // Phone patterns
    public static final String DOMINICAN_PHONE_PATTERN = "^(809|849|829)\\d{7}$";
    public static final String DOMINICAN_PHONE_PATTERN_OPTIONAL = "^(809|849|829)\\d{7}$|^$";

    // Document patterns
    public static final String CEDULA_PATTERN = "\\d{11}$";
    public static final String PASSPORT_PATTERN = "^[0-9A-Z]{9}$";
    public static final String RNC_PATTERN = "^(\\d{9}|\\d{11})$";

    public static final String DATE_PATTERN = "mm/dd/yyyy";

    // Warehouse validation constants
    public static final int MAX_WAREHOUSE_NAME_LENGTH = 50;

    // Length constraints
    public static final int MAX_NOTES_LENGTH = 1000;
    public static final int MAX_REFERENCE_POINTS_LENGTH = 500;
}
