package com.wornux.mapper;

import com.wornux.data.entity.Employee;
import com.wornux.dto.request.EmployeeCreateRequestDto;
import com.wornux.dto.request.EmployeeUpdateRequestDto;

import com.wornux.dto.response.EmployeeListDto;
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

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "employeeRole", source = "employeeRole")
    @Mapping(target = "nationality", source = "nationality")
    @Mapping(target = "province", source = "province")
    @Mapping(target = "municipality", source = "municipality")
    @Mapping(target = "streetAddress", source = "streetAddress")
    @Mapping(target = "profilePicture", source = "profilePicture")
    @Mapping(target = "salary", source = "salary")
    @Mapping(target = "hireDate", source = "hireDate")
    @Mapping(target = "workSchedule", source = "workSchedule")
    @Mapping(target = "emergencyContactName", source = "emergencyContactName")
    @Mapping(target = "emergencyContactPhone", source = "emergencyContactPhone")
    @Mapping(target = "available", source = "available")
    @Mapping(target = "active", source = "active")
    EmployeeListDto toListDto(Employee employee);

    @AfterMapping
    default void validateEmployeeData(@MappingTarget Employee employee) {
        if (employee.getSalary() < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
    }

    EmployeeCreateRequestDto toDTO(Employee employee);
}