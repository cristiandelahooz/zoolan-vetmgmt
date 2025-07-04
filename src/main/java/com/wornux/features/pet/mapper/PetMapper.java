package com.wornux.features.pet.mapper;

import com.wornux.features.pet.domain.Pet;
import com.wornux.features.pet.service.dto.PetCreateDTO;
import com.wornux.features.pet.service.dto.PetUpdateDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = PetMapperHelper.class)
public interface PetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owners", source = "ownerId", qualifiedByName = "mapOwnerAsList")
    Pet toEntity(PetCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "owners", source = "ownerId", qualifiedByName = "mapOwnerAsList")
    void updatePetFromDTO(PetUpdateDTO dto, @MappingTarget Pet pet);

    PetCreateDTO toCreateDTO(Pet savedPet);
}
