package com.wornux.mapper.helper;

import com.wornux.data.entity.Client;
import com.wornux.exception.OwnerNotFoundException;
import com.wornux.services.interfaces.ClientService;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PetMapperHelper {

  @Lazy private final ClientService clientService;

  @Named("mapOwnerAsList")
  public List<Client> mapOwner(Long ownerId) {
    if (ownerId == null) return Collections.emptyList();
    Client client =
        clientService.getClientById(ownerId).orElseThrow(() -> new OwnerNotFoundException(ownerId));
    return List.of(client);
  }
}
