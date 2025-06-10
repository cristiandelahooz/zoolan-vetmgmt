package com.zoolandia.app.bootstrap;

import com.zoolandia.app.features.client.domain.ClientRating;
import com.zoolandia.app.features.client.domain.PreferredContactMethod;
import com.zoolandia.app.features.client.domain.ReferenceSource;
import com.zoolandia.app.features.client.service.ClientService; // Usar la interfaz
import com.zoolandia.app.features.client.service.dto.ClientCreateDTO;
import com.zoolandia.app.features.user.domain.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ClientService clientService; // Interfaz, no implementación

    @Override
    public void run(String... args) throws Exception {
        ClientCreateDTO clientDTO = new ClientCreateDTO();

        // Campos heredados de User
        clientDTO.setUsername("juanperez");
        clientDTO.setPassword("secure123");

        clientDTO.setFirstName("Juan");
        clientDTO.setLastName("Pérez");
        clientDTO.setEmail("juan.perez@email.com");
        clientDTO.setPhoneNumber("+18091234567");
        clientDTO.setBirthDate(LocalDate.of(1985, 5, 15));
        clientDTO.setGender(Gender.MALE);

        // Campos específicos de Client
        clientDTO.setCedula("40212345678");
        clientDTO.setPreferredContactMethod(PreferredContactMethod.EMAIL);
        clientDTO.setAdditionalContactNumbers(Set.of("+18099876543", "+18095551234"));
        clientDTO.setEmergencyContactName("María Pérez");
        clientDTO.setEmergencyContactNumber("+18091111111");
        clientDTO.setRating(ClientRating.BUENO);
        clientDTO.setCreditLimit(5000.0);
        //clientDTO.setCurrentBalance(0.0);
        clientDTO.setPaymentTermsDays(30);
        clientDTO.setNotes("Cliente nuevo, referido por publicidad");
        clientDTO.setReferenceSource(ReferenceSource.REDES_SOCIALES);

        // Dirección
        clientDTO.setProvince("Santo Domingo");
        clientDTO.setMunicipality("Santo Domingo Este");
        clientDTO.setSector("Los Mina");
        clientDTO.setStreetAddress("Calle Principal #123");
        clientDTO.setReferencePoints("Cerca del parque central");

        // Configuraciones
        clientDTO.setReceivesPromotionalInfo(true);
        //clientDTO.setVerified(false);

        // Crear el cliente y guardar el resultado
        var client = clientService.createClient(clientDTO);

// Mostrar datos
        System.out.println("Cliente de prueba creado: " + clientDTO.getFirstName() + " " + clientDTO.getLastName());
        System.out.println("Cliente creado con ID: " + client.getId());


    }
}