package com.wornux.mapper;

import com.wornux.data.entity.Pet;
import com.wornux.dto.request.PetCreateRequestDto;
import com.wornux.dto.request.PetUpdateRequestDto;
import org.mapstruct.*;
import com.wornux.mapper.helper.PetMapperHelper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = PetMapperHelper.class)
public interface PetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owners", source = "ownerId", qualifiedByName = "mapOwnerAsList")
    Pet toEntity(PetCreateRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "owners", source = "ownerId", qualifiedByName = "mapOwnerAsList")
    void updatePetFromDTO(PetUpdateRequestDto dto, @MappingTarget Pet pet);

    PetCreateRequestDto toCreateDTO(Pet savedPet);
}