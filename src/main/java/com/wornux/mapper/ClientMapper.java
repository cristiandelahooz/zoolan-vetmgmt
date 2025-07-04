package com.wornux.mapper;

import com.wornux.domain.Client;
import com.wornux.dto.ClientCreateDTO;
import com.wornux.dto.ClientUpdateDTO;
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
    Client toEntity(ClientCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateClientFromDTO(ClientUpdateDTO dto, @MappingTarget Client client);

    @AfterMapping
    default void validateIdentification(@MappingTarget Client client) {
        if (client.getCedula() == null && client.getPassport() == null) {
            throw new InvalidIdentificationException();
        }
    }

    ClientCreateDTO toDTO(Client client);
}