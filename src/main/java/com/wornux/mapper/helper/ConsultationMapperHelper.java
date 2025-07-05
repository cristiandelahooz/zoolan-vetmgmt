package com.wornux.mapper.helper;

import com.wornux.data.entity.Employee;
import com.wornux.data.entity.Pet;
import com.wornux.service.interfaces.EmployeeService;
import com.wornux.service.interfaces.PetService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsultationMapperHelper {

    @Lazy
    private final PetService petService;
    @Lazy
    private final EmployeeService employeeService;

    @Named("petFromId")
    public Pet petFromId(Long id) {
        if (id == null) {
            return null;
        }
        return petService.getPetById(id).orElse(null);
    }

    @Named("veterinarianFromId")
    public Employee veterinarianFromId(Long id) {
        if (id == null) {
            return null;
        }
        return employeeService.getEmployeeById(id).orElse(null);
    }
}
