package com.wornux.mapper;

import com.wornux.data.entity.Client;
import com.wornux.data.entity.Pet;
import com.wornux.data.entity.WaitingRoom;
import com.wornux.data.repository.ClientRepository;
import com.wornux.data.repository.PetRepository;
import com.wornux.dto.request.WaitingRoomCreateRequestDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WaitingRoomMapper {

    @Mapping(target = "client", source = "dto.clientId")
    @Mapping(target = "pet", source = "dto.petId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ESPERANDO")
    @Mapping(target = "arrivalTime", expression = "java(java.time.LocalDateTime.now())")
    WaitingRoom toEntity(WaitingRoomCreateRequestDto dto, @Context ClientRepository clientRepository,
            @Context PetRepository petRepository);

    default Client mapClient(Long clientId, @Context ClientRepository clientRepository) {
        return clientId == null ? null : clientRepository.findById(clientId).orElseThrow(
                () -> new IllegalArgumentException("Cliente no encontrado con ID: " + clientId));
    }

    default Pet mapPet(Long petId, @Context PetRepository petRepository) {
        return petId == null ? null : petRepository.findById(petId).orElseThrow(() -> new IllegalArgumentException(
                "Mascota no encontrada con ID: " + petId));
    }
}
