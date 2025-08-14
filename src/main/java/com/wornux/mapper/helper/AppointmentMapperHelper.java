package com.wornux.mapper.helper;

import com.wornux.data.entity.Client;
import com.wornux.data.entity.Employee;
import com.wornux.data.entity.Pet;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.EmployeeService;
import com.wornux.services.interfaces.PetService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentMapperHelper {

  private static final String NOT_FOUND_MESSAGE = " not found";
  @Lazy private final ClientService clientService;
  @Lazy private final PetService petService;
  @Lazy private final EmployeeService employeeService;

  @Named("mapClient")
  public Client mapClient(Long clientId) {
    if (clientId == null) {
      return null;
    }
    return clientService
        .getClientById(clientId)
        .orElseThrow(
            () -> new IllegalArgumentException("Client with ID " + clientId + NOT_FOUND_MESSAGE));
  }

  @Named("mapPet")
  public Pet mapPet(Long petId) {
    if (petId == null) {
      return null;
    }
    return petService
        .getPetById(petId)
        .orElseThrow(
            () -> new IllegalArgumentException("Pet with ID " + petId + NOT_FOUND_MESSAGE));
  }

  @Named("mapEmployee")
  public Employee mapEmployee(Long employeeId) {
    if (employeeId == null) {
      return null;
    }
    return employeeService
        .getEmployeeById(employeeId)
        .orElseThrow(
            () ->
                new IllegalArgumentException("Employee with ID " + employeeId + NOT_FOUND_MESSAGE));
  }
}
