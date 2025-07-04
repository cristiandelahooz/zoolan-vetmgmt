package com.wornux.mapper;

import com.wornux.domain.Client;
import com.wornux.service.ClientService;
import com.wornux.exception.OwnerNotFoundException;
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
