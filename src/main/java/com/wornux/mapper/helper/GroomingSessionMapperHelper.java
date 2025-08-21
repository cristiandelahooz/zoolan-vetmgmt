package com.wornux.mapper.helper;

import com.wornux.data.entity.Employee;
import com.wornux.data.entity.Pet;
import com.wornux.services.interfaces.EmployeeService;
import com.wornux.services.interfaces.PetService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroomingSessionMapperHelper {

    private final PetService petService;
    private final EmployeeService employeeService;

    @Named("petFromId")
    public Pet petFromId(Long id) {
        if (id == null) return null;
        return petService.getPetById(id).orElse(null);

    }

    @Named("groomerFromId")
    public Employee groomerFromId(Long id) {
        if (id == null) return null;
        return employeeService.getEmployeeById(id).orElse(null);
    }
}
