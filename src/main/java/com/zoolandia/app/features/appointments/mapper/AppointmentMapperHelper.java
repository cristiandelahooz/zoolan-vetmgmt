
package com.zoolandia.app.features.appointments.mapper;

import com.zoolandia.app.features.client.domain.Client;
import com.zoolandia.app.features.client.service.ClientService;
import com.zoolandia.app.features.employee.domain.Employee;
import com.zoolandia.app.features.employee.service.EmployeeService;
import com.zoolandia.app.features.pet.domain.Pet;
import com.zoolandia.app.features.pet.service.PetService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentMapperHelper {

    private final ClientService clientService;
    private final PetService petService;
    private final EmployeeService employeeService;

    @Named("mapClient")
    public Client mapClient(Long clientId) {
        if (clientId == null) {
            return null;
        }
        return clientService.getClientById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client with ID " + clientId + " not found"));
    }

    @Named("mapPet")
    public Pet mapPet(Long petId) {
        if (petId == null) {
            return null;
        }
        return petService.getPetById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Pet with ID " + petId + " not found"));
    }

    @Named("mapEmployee")
    public Employee mapEmployee(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return employeeService.getEmployeeById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee with ID " + employeeId + " not found"));
    }
}