package com.wornux.mapper;

import com.wornux.data.entity.Client;

import com.wornux.data.entity.Pet;
import com.wornux.dto.request.PetCreateRequestDto;
import com.wornux.dto.request.PetUpdateRequestDto;
import org.mapstruct.*;
import com.wornux.mapper.helper.PetMapperHelper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = PetMapperHelper.class)
public interface PetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owners", source = "dto.ownerId", qualifiedByName = "mapOwnerAsList")
    @Mapping(target = "birthDate", source = "dto.birthDate")
    @Mapping(target = "gender", source = "dto.gender")
    Pet toEntity(PetCreateRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "owners", source = "dto.ownerId", qualifiedByName = "mapOwnerAsList")
    void updatePetFromDTO(PetUpdateRequestDto dto, @MappingTarget Pet pet);

    PetCreateRequestDto toCreateDTO(Pet savedPet);
}