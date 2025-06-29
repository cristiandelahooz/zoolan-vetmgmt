package com.zoolandia.app.features.appointments.dtos;

import com.vaadin.hilla.BrowserCallable;
import com.zoolandia.app.features.appointments.domain.AppointmentStatus;
import com.zoolandia.app.features.appointments.domain.ServiceType;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

@Data
@BrowserCallable
public class AppointmentResponseDTO {
    private Long id;
    private LocalDateTime appointmentDateTime;
    private Integer durationMinutes;
    private ServiceType serviceType;
    private AppointmentStatus status;
    private String reason;
    private String notes;
    
    // Client info
    private Long clientId;
    private String clientName;
    private String clientPhone;
    
    // Pet info
    private Long petId;
    private String petName;
    
    // Employee info
    private Long assignedEmployeeId;
    private String assignedEmployeeName;
    
    // Guest client info
    @Nullable
    private AppointmentClientInfoDTO guestClientInfo;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Calculated fields
    private LocalDateTime endDateTime;
    private String displayName;
    private boolean hasRegisteredClient;
    private boolean requiresVeterinarian;
}