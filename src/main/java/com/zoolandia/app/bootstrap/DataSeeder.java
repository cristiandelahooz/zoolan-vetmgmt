package com.zoolandia.app.bootstrap;

import com.zoolandia.app.features.client.domain.ClientRating;
import com.zoolandia.app.features.client.domain.PreferredContactMethod;
import com.zoolandia.app.features.client.domain.ReferenceSource;
import com.zoolandia.app.features.client.service.ClientService; // Usar la interfaz
import com.zoolandia.app.features.client.service.dto.ClientCreateDTO;
import com.zoolandia.app.features.consultation.service.ConsultationService;
import com.zoolandia.app.features.consultation.service.dto.CreateConsultationDTO;
import com.zoolandia.app.features.employee.domain.Employee;
import com.zoolandia.app.features.employee.domain.EmployeeRole;
import com.zoolandia.app.features.employee.service.EmployeeService;
import com.zoolandia.app.features.employee.service.dto.EmployeeCreateDTO;
import com.zoolandia.app.features.pet.domain.Pet;
import com.zoolandia.app.features.pet.domain.PetType;
import com.zoolandia.app.features.pet.service.PetService;
import com.zoolandia.app.features.pet.service.dto.PetCreateDTO;
import com.zoolandia.app.features.user.domain.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ClientService clientService; // Interfaz, no implementación
    private final PetService petService;
    private final EmployeeService employeeService;
    private final ConsultationService consultationService;



    @Override
    public void run(String... args) throws Exception {
        String email = "juan.perez@email.com";
        String firstName = "Juan";
        String lastName = "Pérez";
        String phoneNumber = "8091234567";
        LocalDate birthDate = LocalDate.of(1985, 5, 15);
        Gender gender = Gender.MALE;
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

        ClientCreateDTO clientDTO = new ClientCreateDTO(email, firstName, lastName, phoneNumber, birthDate, gender,
                nationality, cedula, null, null, null, contactMethod, emergencyName, emergencyPhone, rating,
                creditLimit, paymentTerms, notes, referenceSource, province, municipality, sector, address,
                referencePoints);

        var createdClient = clientService.createClient(clientDTO);


        PetCreateDTO petDTO = new PetCreateDTO();
        petDTO.setName("Max");
        petDTO.setType(PetType.DOG);
        petDTO.setBreed("Labrador Retriever");
        petDTO.setBirthDate(LocalDate.of(2020, 3, 10));
        petDTO.setOwnerId(createdClient.getId());
        petDTO.setGender(com.zoolandia.app.features.pet.domain.Gender.MALE);

        Pet createdPet = petService.createPet(petDTO);



        // Crear empleado veterinario
        EmployeeCreateDTO employeeDTO = new EmployeeCreateDTO();
        employeeDTO.setUsername("drgarcia");
        employeeDTO.setPassword("password123");
        employeeDTO.setFirstName("Ana");
        employeeDTO.setLastName("García");
        employeeDTO.setEmail("ana.garcia@zoolandia.com");
        employeeDTO.setPhoneNumber("8093456789");
        employeeDTO.setBirthDate(LocalDate.of(1990, 8, 25));
        employeeDTO.setGender(Gender.FEMALE);
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

        Employee createdEmployee = employeeService.createEmployee(employeeDTO);


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
}