package com.wornux.mapper;

import com.wornux.data.entity.GroomingSession;
import com.wornux.dto.request.CreateGroomingSessionRequestDto;
import com.wornux.dto.request.UpdateGroomingSessionRequestDto;
import com.wornux.mapper.helper.GroomingSessionMapperHelper;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = GroomingSessionMapperHelper.class
)
public interface GroomingSessionMapper {

    @Mapping(target = "pet", source = "dto.petId", qualifiedByName = "petFromId")
    @Mapping(target = "groomer", source = "dto.groomerId", qualifiedByName = "groomerFromId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true) // se setea por @Builder.Default o lógica de servicio
    GroomingSession toEntity(CreateGroomingSessionRequestDto dto);

    // Igual que en ConsultationMapper usas el "Create*" como DTO de salida.
    @Mapping(target = "petId", source = "pet.id")
    @Mapping(target = "groomerId", source = "groomer.id")
    CreateGroomingSessionRequestDto toDTO(GroomingSession entity);

    List<CreateGroomingSessionRequestDto> toDTOList(List<GroomingSession> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pet", source = "dto.petId", qualifiedByName = "petFromId")
    @Mapping(target = "groomer", source = "dto.groomerId", qualifiedByName = "groomerFromId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void partialUpdate(@MappingTarget GroomingSession entity, UpdateGroomingSessionRequestDto dto);
}
