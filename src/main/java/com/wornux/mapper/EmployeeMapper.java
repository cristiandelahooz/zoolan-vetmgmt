package com.wornux.mapper;

import com.wornux.data.entity.Employee;
import com.wornux.dto.request.EmployeeCreateRequestDto;
import com.wornux.dto.request.EmployeeUpdateRequestDto;
import org.mapstruct.*;

/** Mapper for the {@link Employee} entity and its DTOs. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "systemRole", expression = "java(dto.getEmployeeRole().getSystemRole())")
  Employee toEntity(EmployeeCreateRequestDto dto);

  @Mapping(target = "id", ignore = true)
  void updateEmployeeFromDto(EmployeeUpdateRequestDto dto, @MappingTarget Employee entity);

  EmployeeCreateRequestDto toDTO(Employee employee);

  @Mapping(target = "id", source = "id")
  EmployeeUpdateRequestDto toUpdateDTO(Employee employee);
}
