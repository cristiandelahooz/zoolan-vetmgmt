package com.wornux.mapper;

import com.wornux.data.entity.Consultation;
import com.wornux.dto.request.CreateConsultationRequestDto;
import com.wornux.data.entity.Employee;
import com.wornux.service.EmployeeService;
import com.wornux.data.entity.Pet;
import com.wornux.service.PetService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import com.wornux.dto.request.UpdateConsultationRequestDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ConsultationMapper {

    @Autowired
    protected PetService petService;

    @Autowired
    protected EmployeeService employeeService;

    @Mapping(target = "pet", source = "petId")
    @Mapping(target = "veterinarian", source = "veterinarianId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Consultation toEntity(CreateConsultationRequestDto dto);

    @Mapping(target = "petId", source = "pet.id")
    @Mapping(target = "veterinarianId", source = "veterinarian.id")
    public abstract CreateConsultationRequestDto toDTO(Consultation entity);

    public abstract List<CreateConsultationRequestDto> toDTOList(List<Consultation> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pet", source = "petId")
    @Mapping(target = "veterinarian", source = "veterinarianId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void partialUpdate(@MappingTarget Consultation entity, UpdateConsultationRequestDto dto);

    protected Pet petFromId(Long id) {
        if (id == null) {
            return null;
        }
        return petService.getPetById(id).orElse(null);
    }

    protected Employee veterinarianFromId(Long id) {
        if (id == null) {
            return null;
        }
        return employeeService.getEmployeeById(id).orElse(null);
    }
}