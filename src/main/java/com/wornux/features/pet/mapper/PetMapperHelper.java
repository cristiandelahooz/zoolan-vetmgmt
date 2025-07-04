package com.wornux.features.pet.mapper;

import com.wornux.features.client.domain.Client;
import com.wornux.features.client.service.ClientService;
import com.wornux.features.pet.service.exception.OwnerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PetMapperHelper {

    private final ClientService clientService;

    @Named("mapOwner")
    public Client mapOwner(Long ownerId) {
        if (ownerId == null)
            return null;
        return clientService.getClientById(ownerId).orElseThrow(() -> new OwnerNotFoundException(ownerId));
    }
}
