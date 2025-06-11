package com.zoolandia.app.features.employee.mapper;

import com.zoolandia.app.features.employee.domain.Employee;
import com.zoolandia.app.features.employee.service.dto.EmployeeCreateDTO;
import com.zoolandia.app.features.employee.service.dto.EmployeeUpdateDTO;

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
    Employee toEntity(EmployeeCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employeeRole", source = "employeeRole")
    @Mapping(target = "systemRole", expression = "java(dto.getEmployeeRole() != null ? dto.getEmployeeRole().getSystemRole() : employee.getSystemRole())")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "emergencyContactName", source = "emergencyContactName")
    @Mapping(target = "emergencyContactPhone", source = "emergencyContactPhone")
    void partialUpdate(@MappingTarget Employee employee, EmployeeUpdateDTO dto);

    @AfterMapping
    default void validateEmployeeData(@MappingTarget Employee employee) {
        if (employee.getSalary() != null && employee.getSalary() < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }

        if (employee.getHireDate() == null) {
            employee.setHireDate(java.time.LocalDate.now());
        }

        if (employee.getEmployeeRole() != null && employee.getRole() == null) {
            employee.setRole(employee.getEmployeeRole().getSystemRole());
        }
    }

    EmployeeCreateDTO toDTO(Employee employee);
}