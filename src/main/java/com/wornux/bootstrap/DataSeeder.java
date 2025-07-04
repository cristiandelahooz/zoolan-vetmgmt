package com.wornux.bootstrap;

import static com.wornux.dto.Gender.*;

import com.wornux.features.appointments.domain.ServiceType;
import com.wornux.features.appointments.dtos.AppointmentCreateDTO;
import com.wornux.features.appointments.service.AppointmentService;
import com.wornux.features.client.domain.ClientRating;
import com.wornux.features.client.domain.PreferredContactMethod;
import com.wornux.features.client.domain.ReferenceSource;
import com.wornux.features.client.service.ClientService;
import com.wornux.features.client.service.dto.ClientCreateDTO;
import com.wornux.features.consultation.service.ConsultationService;
import com.wornux.features.consultation.service.dto.CreateConsultationDTO;
import com.wornux.features.employee.domain.Employee;
import com.wornux.features.employee.domain.EmployeeRole;
import com.wornux.features.employee.service.EmployeeService;
import com.wornux.features.employee.service.dto.EmployeeCreateDTO;
import com.wornux.features.pet.domain.Pet;
import com.wornux.features.pet.domain.PetType;
import com.wornux.features.pet.service.PetService;
import com.wornux.features.pet.service.dto.PetCreateDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final ClientService clientService;
  private final EmployeeService employeeService;
  private final PetService petService;
  private final AppointmentService appointmentService;
  private final ConsultationService consultationService;

  @Override
  public void run(String... args) throws Exception {
    populateClients();
    Employee createdEmployee = populateEmployees();
    Pet createdPet = populatePets();
    populateAppointments();
    populateConsultations(createdEmployee, createdPet);
  }

  private void populateConsultations(Employee createdEmployee, Pet createdPet) {
    CreateConsultationDTO consultationDTO = new CreateConsultationDTO();
    consultationDTO.setNotes("Consulta de rutina. Mascota en buen estado general.");
    consultationDTO.setDiagnosis("Examen físico normal");
    consultationDTO.setTreatment("Continuar con dieta balanceada y ejercicio regular");
    consultationDTO.setPrescription("Vitaminas multiples - 1 tableta diaria por 30 días");
    consultationDTO.setConsultationDate(LocalDateTime.now());
    consultationDTO.setPetId(createdPet.getId());
    consultationDTO.setVeterinarianId(createdEmployee.getId());

    consultationService.create(consultationDTO);

    CreateConsultationDTO anotherConsultationDTO = new CreateConsultationDTO();
    anotherConsultationDTO.setNotes("Consulta de rutina. Mascota en buen estado general.");
    anotherConsultationDTO.setDiagnosis("Examen físico anormal");
    anotherConsultationDTO.setTreatment("Continuar con dieta balanceada y ejercicio regular");
    anotherConsultationDTO.setPrescription("Vitaminas multiples - 2 tableta diaria por 30 días");
    anotherConsultationDTO.setConsultationDate(LocalDateTime.now());
    anotherConsultationDTO.setPetId(createdPet.getId());
    anotherConsultationDTO.setVeterinarianId(createdEmployee.getId());

    consultationService.create(anotherConsultationDTO);
  }

  private void populateAppointments() {
    AppointmentCreateDTO appointmentDTO = new AppointmentCreateDTO();
    appointmentDTO.setPetId(1L);
    appointmentDTO.setClientId(1L);
    appointmentDTO.setAssignedEmployeeId(2L);
    appointmentDTO.setStartAppointmentDate(LocalDateTime.of(2025, 7, 4, 10, 0));
    appointmentDTO.setEndAppointmentDate(LocalDateTime.of(2025, 7, 4, 11, 0));
    appointmentDTO.setServiceType(ServiceType.DESPARASITACION);
    appointmentService.createAppointment(appointmentDTO);
  }

  private Pet populatePets() {
    PetCreateDTO petDTO = new PetCreateDTO();
    petDTO.setName("Max");
    petDTO.setBreed("Labrador Retriever");
    petDTO.setType(PetType.DOG);
    petDTO.setBirthDate(LocalDate.of(2020, 6, 15));
    petDTO.setOwnerId(1L);
    petDTO.setGender(MALE);
    return petService.createPet(petDTO);
  }

  private Employee populateEmployees() {
    EmployeeCreateDTO employeeDTO = new EmployeeCreateDTO();
    employeeDTO.setUsername("drgarcia");
    employeeDTO.setPassword("password123");
    employeeDTO.setFirstName("Ana");
    employeeDTO.setLastName("García");
    employeeDTO.setEmail("ana.garcia@zoolandia.com");
    employeeDTO.setPhoneNumber("8093456789");
    employeeDTO.setBirthDate(LocalDate.of(1990, 8, 25));
    employeeDTO.setGender(FEMALE);
    employeeDTO.setNationality("Dominicana");
    employeeDTO.setProvince("Santo Domingo");
    employeeDTO.setMunicipality("Santo Domingo Norte");
    employeeDTO.setSector("Villa Mella");
    employeeDTO.setStreetAddress("Av. Charles de Gaulle #456");
    employeeDTO.setEmployeeRole(EmployeeRole.VETERINARIAN);
    employeeDTO.setSalary(120000.0);
    employeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
    employeeDTO.setAvailable(true);
    employeeDTO.setActive(true);
    employeeDTO.setWorkSchedule("Lunes a Viernes 8:00 AM - 5:00 PM");
    employeeDTO.setEmergencyContactName("Luis García");
    employeeDTO.setEmergencyContactPhone("8094567890");

    return employeeService.createEmployee(employeeDTO);
  }

  private void populateClients() {
    String email = "juan.perez@email.com";
    String firstName = "Juan";
    String lastName = "Pérez";
    String phoneNumber = "8091234567";
    LocalDate birthDate = LocalDate.of(1985, 5, 15);
    String nationality = "Domiciliado en el Estado Dominicano";
    String cedula = "40212345678";
    PreferredContactMethod contactMethod = PreferredContactMethod.EMAIL;
    String emergencyName = "María Pérez";
    String emergencyPhone = "8091111111";
    ClientRating rating = ClientRating.BUENO;
    Double creditLimit = 5000.0;
    Integer paymentTerms = 30;
    String notes = "Cliente nuevo, referido por publicidad";
    ReferenceSource referenceSource = ReferenceSource.REDES_SOCIALES;
    String province = "Santo Domingo";
    String municipality = "Santo Domingo Este";
    String sector = "Los Mina";
    String address = "Calle Principal #123";
    String referencePoints = "Cerca del parque central";

    ClientCreateDTO clientDTO =
        new ClientCreateDTO(
            email,
            firstName,
            lastName,
            phoneNumber,
            birthDate,
            MALE,
            nationality,
            cedula,
            null,
            null,
            null,
            contactMethod,
            emergencyName,
            emergencyPhone,
            rating,
            creditLimit,
            paymentTerms,
            notes,
            referenceSource,
            province,
            municipality,
            sector,
            address,
            referencePoints);

    clientService.createClient(clientDTO);
  }
}

