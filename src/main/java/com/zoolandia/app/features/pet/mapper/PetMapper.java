package com.zoolandia.app.features.pet.mapper;

import com.zoolandia.app.features.pet.domain.Pet;
import com.zoolandia.app.features.pet.service.dto.PetCreateDTO;
import com.zoolandia.app.features.pet.service.dto.PetUpdateDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = PetMapperHelper.class)
public interface PetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", source = "ownerId", qualifiedByName = "mapOwner")
    Pet toEntity(PetCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "owner", source = "ownerId", qualifiedByName = "mapOwner")
    void updatePetFromDTO(PetUpdateDTO dto, @MappingTarget Pet pet);

    PetCreateDTO toCreateDTO(Pet savedPet);
}
