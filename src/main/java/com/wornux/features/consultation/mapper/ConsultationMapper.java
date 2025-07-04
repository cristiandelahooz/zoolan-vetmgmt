package com.wornux.features.consultation.mapper;

import com.wornux.features.consultation.domain.Consultation;
import com.wornux.features.consultation.service.dto.CreateConsultationDTO;
import com.wornux.features.employee.domain.Employee;
import com.wornux.features.employee.service.EmployeeService;
import com.wornux.features.pet.domain.Pet;
import com.wornux.features.pet.service.PetService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
    public abstract Consultation toEntity(CreateConsultationDTO dto);

    @Mapping(target = "petId", source = "pet.id")
    @Mapping(target = "veterinarianId", source = "veterinarian.id")
    public abstract CreateConsultationDTO toDTO(Consultation entity);

    public abstract List<CreateConsultationDTO> toDTOList(List<Consultation> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pet", source = "petId")
    @Mapping(target = "veterinarian", source = "veterinarianId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void partialUpdate(@MappingTarget Consultation entity, CreateConsultationDTO dto);

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