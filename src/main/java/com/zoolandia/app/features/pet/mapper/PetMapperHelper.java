package com.zoolandia.app.features.pet.mapper;

import com.zoolandia.app.features.client.domain.Client;
import com.zoolandia.app.features.client.repository.ClientRepository;
import com.zoolandia.app.features.pet.service.exception.OwnerNotFoundException;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PetMapperHelper {

  @Autowired private ClientRepository clientRepository;

  @Named("mapOwner")
  public Client mapOwner(Long ownerId) {
    if (ownerId == null) return null;
    return clientRepository
        .findById(ownerId)
        .orElseThrow(() -> new OwnerNotFoundException(ownerId));
  }
}
