package com.wornux.bootstrap;

import static com.wornux.data.enums.Gender.*;

import com.wornux.data.enums.ServiceType;
import com.wornux.dto.request.*;
import com.wornux.service.interfaces.AppointmentService;
import com.wornux.data.enums.ClientRating;
import com.wornux.data.enums.PreferredContactMethod;
import com.wornux.data.enums.ReferenceSource;
import com.wornux.service.interfaces.ClientService;
import com.wornux.service.interfaces.ConsultationService;
import com.wornux.dto.request.CreateConsultationRequestDto;
import com.wornux.data.entity.Employee;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.service.interfaces.EmployeeService;
import com.wornux.data.entity.Pet;
import com.wornux.data.enums.PetType;
import com.wornux.service.interfaces.PetService;

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
        CreateConsultationRequestDto consultationDTO = CreateConsultationRequestDto.builder()
                .notes("Consulta de rutina. Mascota en buen estado general.")
                .diagnosis("Examen físico normal")
                .treatment("Continuar con dieta balanceada y ejercicio regular")
                .prescription("Vitaminas multiples - 1 tableta diaria por 30 días")
                .consultationDate(LocalDateTime.now())
                .petId(createdPet.getId())
                .veterinarianId(createdEmployee.getId())
                .build();

        consultationService.create(consultationDTO);

        CreateConsultationRequestDto anotherConsultationDTO = CreateConsultationRequestDto.builder()
                .notes("Consulta de rutina. Mascota en buen estado general.")
                .diagnosis("Examen físico anormal")
                .treatment("Continuar con dieta balanceada y ejercicio regular")
                .prescription("Vitaminas multiples - 2 tableta diaria por 30 días")
                .consultationDate(LocalDateTime.now())
                .petId(createdPet.getId())
                .veterinarianId(createdEmployee.getId())
                .build();

        consultationService.create(anotherConsultationDTO);
    }

    private void populateAppointments() {
        AppointmentCreateRequestDto appointmentDTO = AppointmentCreateRequestDto.builder()
                .petId(1L)
                .clientId(1L)
                .assignedEmployeeId(2L)
                .startAppointmentDate(LocalDateTime.now().plusDays(1))
                .endAppointmentDate(LocalDateTime.now().plusDays(1).plusHours(1))
                .serviceType(ServiceType.CONSULTA_GENERAL)
                .build();
        appointmentService.createAppointment(appointmentDTO);
    }

    private Pet populatePets() {
        PetCreateRequestDto petDTO = PetCreateRequestDto.builder()
                .name("Max")
                .breed("Labrador Retriever")
                .type(PetType.DOG)
                .birthDate(LocalDate.of(2020, 6, 15))
                .ownerId(1L)
                .gender(MALE)
                .build();
        return petService.createPet(petDTO);
    }

    private Employee populateEmployees() {
        EmployeeCreateRequestDto employeeDTO = EmployeeCreateRequestDto.builder()
                .username("drgarcia")
                .password("password123")
                .firstName("Ana")
                .lastName("García")
                .email("ana.garcia@zoolandia.com")
                .phoneNumber("8093456789")
                .birthDate(LocalDate.of(1990, 8, 25))
                .gender(FEMALE)
                .nationality("Dominicana")
                .province("Santo Domingo")
                .municipality("Santo Domingo Norte")
                .sector("Villa Mella")
                .streetAddress("Av. Charles de Gaulle #456")
                .employeeRole(EmployeeRole.VETERINARIAN)
                .salary(120000.0)
                .hireDate(LocalDate.of(2023, 1, 15))
                .available(true)
                .active(true)
                .workSchedule("Lunes a Viernes 8:00 AM - 5:00 PM")
                .emergencyContactName("Luis García")
                .emergencyContactPhone("8094567890")
                .build();

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

        ClientCreateRequestDto clientDTO = new ClientCreateRequestDto(email, firstName, lastName, phoneNumber, birthDate, MALE,
                nationality, cedula, null, null, null, contactMethod, emergencyName, emergencyPhone, rating,
                creditLimit, paymentTerms, notes, referenceSource, province, municipality, sector, address,
                referencePoints);

        clientService.createClient(clientDTO);
    }
}
