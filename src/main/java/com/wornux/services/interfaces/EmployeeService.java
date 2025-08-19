package com.wornux.services.interfaces;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.data.entity.Employee;
import com.wornux.data.repository.EmployeeRepository;
import com.wornux.dto.request.EmployeeCreateRequestDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.wornux.dto.request.EmployeeUpdateRequestDto;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Service Interface for managing {@link Employee} entities.
 */
@BrowserCallable
@Validated
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface EmployeeService {

    /**
     * Retrieves an Employee by ID.
     *
     * @param id the ID of the employee
     * @return the Employee entity if found
     */
    Optional<Employee> getEmployeeById(Long id);

    /**
     * Retrieves all Employees with pagination.
     *
     * @param pageable pagination information
     * @return paginated list of Employees
     */
    Page<Employee> getAllEmployees(Pageable pageable);

    /**
     * Retrieves all employees who are veterinarians.
     *
     * @return list of veterinarian employees
     */
    List<Employee> getVeterinarians();

    /**
     * Retrieves the Employee repository for direct database operations.
     *
     * @return the EmployeeRepository instance
     */
    EmployeeRepository getRepository();

    /**
     * Saves a new Employee entity.
     *
     * @param value the EmployeeCreateRequestDto containing employee data
     * @return the saved EmployeeCreateRequestDto
     */
    EmployeeCreateRequestDto save(@NonNull EmployeeCreateRequestDto value);

    void delete(@NonNull Long id);

    void updateEmployee(@NonNull Long id, @Valid EmployeeUpdateRequestDto dto);

    List<Employee> getGroomers();
}
