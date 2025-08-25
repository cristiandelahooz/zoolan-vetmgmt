package com.wornux.mapper;

import com.wornux.data.entity.Employee;
import com.wornux.data.entity.WorkScheduleDay;
import com.wornux.dto.request.EmployeeCreateRequestDto;
import com.wornux.dto.request.EmployeeUpdateRequestDto;
import com.wornux.dto.request.WorkScheduleDayDto;
import java.util.List;
import org.mapstruct.*;

/** Mapper for the {@link Employee} entity and its DTOs. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "systemRole", expression = "java(dto.getEmployeeRole().getSystemRole())")
  @Mapping(target = "workScheduleDays", source = "workScheduleDays")
  Employee toEntity(EmployeeCreateRequestDto dto);

  @Mapping(target = "id", ignore = true)
  void updateEmployeeFromDto(EmployeeUpdateRequestDto dto, @MappingTarget Employee entity);

  // Custom mapping method for workScheduleDays
  void updateWorkScheduleDayFromDto(WorkScheduleDayDto dto, @MappingTarget WorkScheduleDay entity);

  @AfterMapping
  default void updateWorkScheduleDays(
      EmployeeUpdateRequestDto dto, @MappingTarget Employee entity) {
    if (dto.getWorkScheduleDays() != null) {
      // Clear existing work schedule days
      entity.getWorkScheduleDays().clear();
      // Add new work schedule days from DTO
      dto.getWorkScheduleDays()
          .forEach(
              workScheduleDayDto -> {
                entity.getWorkScheduleDays().add(toEntity(workScheduleDayDto));
              });
    }
  }

  @Mapping(target = "workScheduleDays", source = "workScheduleDays")
  EmployeeCreateRequestDto toDTO(Employee employee);

  // Mapping methods for WorkScheduleDay
  WorkScheduleDay toEntity(WorkScheduleDayDto dto);

  WorkScheduleDayDto toDto(WorkScheduleDay entity);

  List<WorkScheduleDay> toEntityList(List<WorkScheduleDayDto> dtoList);

  List<WorkScheduleDayDto> toDtoList(List<WorkScheduleDay> entityList);
}
