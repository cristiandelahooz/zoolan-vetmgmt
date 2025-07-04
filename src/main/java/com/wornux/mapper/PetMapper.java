package com.wornux.mapper;

import com.wornux.domain.Pet;
import com.wornux.dto.PetCreateDTO;
import com.wornux.dto.PetUpdateDTO;
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
