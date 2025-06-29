package com.zoolandia.app.common.constants;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AppointmentConstants {
    
    // Default appointment duration
    public static final int DEFAULT_APPOINTMENT_DURATION_MINUTES = 60;
    
    // Validation constraints
    public static final int MAX_REASON_LENGTH = 500;
    public static final int MAX_APPOINTMENT_NOTES_LENGTH = 1000;
    public static final int MAX_GUEST_CLIENT_NAME_LENGTH = 100;
    public static final int MAX_GUEST_CLIENT_PHONE_LENGTH = 15;
    public static final int MAX_GUEST_CLIENT_EMAIL_LENGTH = 100;
    
    // Patterns
    public static final String APPOINTMENT_TIME_PATTERN = "HH:mm";
    
    // Business rules
    public static final int MIN_APPOINTMENT_DURATION_MINUTES = 15;
    public static final int MAX_APPOINTMENT_DURATION_MINUTES = 480; // 8 hours
}