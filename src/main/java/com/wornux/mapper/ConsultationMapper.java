package com.wornux.mapper;

import com.wornux.data.entity.Consultation;
import com.wornux.dto.request.CreateConsultationRequestDto;
import com.wornux.dto.request.UpdateConsultationRequestDto;
import com.wornux.mapper.helper.ConsultationMapperHelper;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ConsultationMapperHelper.class)
public interface ConsultationMapper {

    @Mapping(target = "pet", source = "dto.petId", qualifiedByName = "petFromId")
    @Mapping(target = "veterinarian", source = "dto.veterinarianId", qualifiedByName = "veterinarianFromId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Consultation toEntity(CreateConsultationRequestDto dto);

    @Mapping(target = "petId", source = "pet.id")
    @Mapping(target = "veterinarianId", source = "veterinarian.id")
    CreateConsultationRequestDto toDTO(Consultation entity);

    List<CreateConsultationRequestDto> toDTOList(List<Consultation> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pet", source = "dto.petId", qualifiedByName = "petFromId")
    @Mapping(target = "veterinarian", source = "dto.veterinarianId", qualifiedByName = "veterinarianFromId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void partialUpdate(@MappingTarget Consultation entity, UpdateConsultationRequestDto dto);
}
