package com.wornux.mapper;

import com.wornux.data.entity.Employee;
import com.wornux.dto.request.EmployeeCreateRequestDto;
import com.wornux.dto.request.EmployeeUpdateRequestDto;

import org.mapstruct.*;

/**
 * Mapper for the {@link Employee} entity and its DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "employeeRole", source = "employeeRole")
    @Mapping(target = "systemRole", expression = "java(dto.getEmployeeRole().getSystemRole())")
    @Mapping(target = "emergencyContactName", source = "emergencyContactName")
    @Mapping(target = "emergencyContactPhone", source = "emergencyContactPhone")
    Employee toEntity(EmployeeCreateRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employeeRole", source = "employeeRole")
    @Mapping(target = "systemRole", expression = "java(dto.getEmployeeRole() != null ? dto.getEmployeeRole().getSystemRole() : employee.getSystemRole())")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "emergencyContactName", source = "emergencyContactName")
    @Mapping(target = "emergencyContactPhone", source = "emergencyContactPhone")
    void partialUpdate(@MappingTarget Employee employee, EmployeeUpdateRequestDto dto);

    @AfterMapping
    default void validateEmployeeData(@MappingTarget Employee employee) {
        if (employee.getSalary() < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
    }

    EmployeeCreateRequestDto toDTO(Employee employee);
}