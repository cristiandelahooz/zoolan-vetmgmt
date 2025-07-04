package com.wornux.features.pet.mapper;

import com.wornux.features.client.domain.Client;
import com.wornux.features.client.service.ClientService;
import com.wornux.features.pet.service.exception.OwnerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PetMapperHelper {

    private final ClientService clientService;

    @Named("mapOwnerAsList")
    public List<Client> mapOwner(Long ownerId) {
        if (ownerId == null)
            return Collections.emptyList();
        Client client = clientService.getClientById(ownerId).orElseThrow(() -> new OwnerNotFoundException(ownerId));
        return List.of(client);
    }
}
