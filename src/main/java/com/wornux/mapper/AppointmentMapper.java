package com.wornux.mapper;

import com.wornux.domain.Appointment;
import com.wornux.dto.AppointmentCreateDTO;
import com.wornux.dto.AppointmentResponseDTO;
import com.wornux.dto.AppointmentUpdateDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = AppointmentMapperHelper.class)
public interface AppointmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PROGRAMADA")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "client", source = "clientId", qualifiedByName = "mapClient")
    @Mapping(target = "pet", source = "petId", qualifiedByName = "mapPet")
    @Mapping(target = "assignedEmployee", source = "assignedEmployeeId", qualifiedByName = "mapEmployee")
    Appointment toEntity(AppointmentCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "client", source = "clientId", qualifiedByName = "mapClient")
    @Mapping(target = "pet", source = "petId", qualifiedByName = "mapPet")
    @Mapping(target = "assignedEmployee", source = "assignedEmployeeId", qualifiedByName = "mapEmployee")
    void updateAppointmentFromDTO(AppointmentUpdateDTO dto, @MappingTarget Appointment appointment);

    @Mapping(target = "eventId", source = "appointment.id")
    @Mapping(target = "appointmentTitle", expression = "java(appointment.getAppointmentTitle())")
    @Mapping(target = "clientName", expression = "java(appointment.getClientDisplayName())")
    @Mapping(target = "clientContactPhone", expression = "java(appointment.getClientContactPhone())")
    @Mapping(target = "petName", source = "pet.name")
    @Mapping(target = "assignedEmployeeName", expression = "java(appointment.getEmployeeDisplayName())")
    @Mapping(target = "startAppointmentDate", expression = "java(appointment.getStartAppointmentDate())")
    @Mapping(target = "completed", expression = "java(appointment.isCompleted())")
    @Mapping(target = "cancelled", expression = "java(appointment.isCancelled())")
    @Mapping(target = "hasRegisteredClient", expression = "java(appointment.hasRegisteredClient())")
    @Mapping(target = "requiresVeterinarian", expression = "java(appointment.requiresVeterinarian())")
    AppointmentResponseDTO toResponseDTO(Appointment appointment);
}