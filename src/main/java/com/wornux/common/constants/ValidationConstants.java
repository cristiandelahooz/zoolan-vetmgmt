package com.wornux.common.constants;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ValidationConstants {
    // Client validation constants
    public static final int MAX_IDENTIFICATION_DOCUMENT_COUNT = 1;
    public static final int MIN_IDENTIFICATION_DOCUMENT_COUNT = 1;

    // Phone patterns
    public static final String DOMINICAN_PHONE_PATTERN = "^(809|849|829)\\d{7}$";

    // Document patterns
    public static final String CEDULA_PATTERN = "\\d{11}$";
    public static final String PASSPORT_PATTERN = "^[0-9A-Z]{9}$";
    public static final String RNC_PATTERN = "\\d{9}$";

    public static final String DATE_PATTERN = "yyyy-MM-dd";

    // Length constraints
    public static final int MAX_NOTES_LENGTH = 1000;
    public static final int MAX_REFERENCE_POINTS_LENGTH = 500;
}
