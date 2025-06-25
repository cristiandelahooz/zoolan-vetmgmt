package com.zoolandia.app.features.pet.mapper;

import com.zoolandia.app.features.client.domain.Client;
import com.zoolandia.app.features.client.repository.ClientRepository;
import com.zoolandia.app.features.pet.service.exception.OwnerNotFoundException;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PetMapperHelper {

    @Autowired
    private ClientRepository clientRepository;

    @Named("mapOwnerAsList")
    public List<Client> mapOwnerAsList(Long ownerId) {
        if (ownerId == null) return Collections.emptyList();
        Client client = clientRepository.findById(ownerId)
                .orElseThrow(() -> new OwnerNotFoundException(ownerId));
        return List.of(client);
    }

}
