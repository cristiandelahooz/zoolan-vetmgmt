package com.wornux.mapper;

import com.wornux.data.entity.Pet;
import com.wornux.dto.request.PetCreateRequestDto;
import com.wornux.dto.request.PetUpdateRequestDto;
import com.wornux.mapper.helper.PetMapperHelper;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = PetMapperHelper.class)
public interface PetMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "owners", source = "dto.ownerId", qualifiedByName = "mapOwnerAsList")
  @Mapping(target = "birthDate", source = "dto.birthDate")
  @Mapping(target = "gender", source = "dto.gender")
  @Mapping(target = "color", source = "dto.color")
  @Mapping(target = "size", source = "dto.size")
  @Mapping(target = "furType", source = "dto.furType")
  Pet toEntity(PetCreateRequestDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "owners", ignore = true)
  void updatePetFromDTO(PetUpdateRequestDto dto, @MappingTarget Pet pet);

  PetCreateRequestDto toCreateDTO(Pet savedPet);
}
