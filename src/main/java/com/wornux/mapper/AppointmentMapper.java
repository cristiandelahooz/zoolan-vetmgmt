package com.wornux.mapper;

import com.wornux.data.entity.Client;
import com.wornux.data.entity.Pet;
import com.wornux.data.entity.Employee;

import com.wornux.data.entity.Appointment;
import com.wornux.dto.request.AppointmentCreateRequestDto;
import com.wornux.dto.request.AppointmentUpdateRequestDto;
import com.wornux.dto.response.AppointmentResponseDto;
import org.mapstruct.*;
import com.wornux.mapper.helper.AppointmentMapperHelper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = AppointmentMapperHelper.class)
public interface AppointmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PROGRAMADA")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "notes", source = "dto.notes")
    @Mapping(target = "client", source = "dto.clientId", qualifiedByName = "mapClient")
    @Mapping(target = "pet", source = "dto.petId", qualifiedByName = "mapPet")
    @Mapping(target = "assignedEmployee", source = "dto.assignedEmployeeId", qualifiedByName = "mapEmployee")
    Appointment toEntity(AppointmentCreateRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", source = "dto.eventId")
    @Mapping(target = "client", source = "dto.clientId", qualifiedByName = "mapClient")
    @Mapping(target = "pet", source = "dto.petId", qualifiedByName = "mapPet")
    @Mapping(target = "assignedEmployee", source = "dto.assignedEmployeeId", qualifiedByName = "mapEmployee")
    void updateAppointmentFromDTO(AppointmentUpdateRequestDto dto, @MappingTarget Appointment appointment);

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
    AppointmentResponseDto toResponseDTO(Appointment appointment);
}
