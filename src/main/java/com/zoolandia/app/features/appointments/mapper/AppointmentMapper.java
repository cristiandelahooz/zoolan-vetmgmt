package com.zoolandia.app.features.appointments.mapper;

import com.zoolandia.app.features.appointments.domain.Appointment;
import com.zoolandia.app.features.appointments.dtos.AppointmentCreateDTO;
import com.zoolandia.app.features.appointments.dtos.AppointmentResponseDTO;
import com.zoolandia.app.features.appointments.dtos.AppointmentUpdateDTO;
import com.zoolandia.app.features.client.domain.Client;
import com.zoolandia.app.features.client.service.ClientService;
import com.zoolandia.app.features.employee.domain.Employee;
import com.zoolandia.app.features.employee.service.EmployeeService;
import com.zoolandia.app.features.pet.domain.Pet;
import com.zoolandia.app.features.pet.service.PetService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@RequiredArgsConstructor
public abstract class AppointmentMapper {
    private ClientService clientService;
    private PetService petService;
    private EmployeeService employeeService;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PROGRAMADA")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "client", source = "clientId", qualifiedByName = "mapClient")
    @Mapping(target = "pet", source = "petId", qualifiedByName = "mapPet")
    @Mapping(target = "assignedEmployee", source = "assignedEmployeeId", qualifiedByName = "mapEmployee")
    public abstract Appointment toEntity(AppointmentCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "client", source = "clientId", qualifiedByName = "mapClient")
    @Mapping(target = "pet", source = "petId", qualifiedByName = "mapPet")
    @Mapping(target = "assignedEmployee", source = "assignedEmployeeId", qualifiedByName = "mapEmployee")
    public abstract void updateAppointmentFromDTO(AppointmentUpdateDTO dto, @MappingTarget Appointment appointment);

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
    public abstract AppointmentResponseDTO toResponseDTO(Appointment appointment);

    @Named("mapClient")
    protected Client mapClient(Long clientId) {
        if (clientId == null) {
            return null;
        }
        return clientService.getClientById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client with ID " + clientId + " not found"));
    }

    @Named("mapPet")
    protected Pet mapPet(Long petId) {
        if (petId == null) {
            return null;
        }
        return petService.getPetById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Pet with ID " + petId + " not found"));
    }

    @Named("mapEmployee")
    protected Employee mapEmployee(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return employeeService.getEmployeeById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee with ID " + employeeId + " not found"));
    }

}