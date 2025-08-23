package com.wornux.mapper;

import com.wornux.data.entity.Offering;
import com.wornux.dto.request.ServiceCreateRequestDto;
import com.wornux.dto.request.ServiceUpdateRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ServiceMapper {

  Offering toEntity(ServiceCreateRequestDto dto);

  void updateServiceFromDto(ServiceUpdateRequestDto dto, @MappingTarget Offering offering);
}
