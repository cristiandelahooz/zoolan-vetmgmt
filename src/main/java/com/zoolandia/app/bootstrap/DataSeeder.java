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

        clientService.createClient(clientDTO);
    }
}