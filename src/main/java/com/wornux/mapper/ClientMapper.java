package com.wornux.mapper;

import com.wornux.data.entity.Client;
import com.wornux.dto.request.ClientCreateRequestDto;
import com.wornux.dto.request.ClientUpdateRequestDto;
import com.wornux.exception.InvalidIdentificationException;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "verified", constant = "false")
    @Mapping(target = "currentBalance", constant = "0.0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Client toEntity(ClientCreateRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateClientFromDTO(ClientUpdateRequestDto dto, @MappingTarget Client client);

    @AfterMapping
    default void validateIdentification(@MappingTarget Client client) {
        if (client.getCedula() == null && client.getPassport() == null && client.getRnc() == null) {
            throw new InvalidIdentificationException();
        }
    }

    ClientCreateRequestDto toDTO(Client client);
}