package com.wornux.services.interfaces;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.data.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.Optional;

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
     * @param id
     *            the ID of the employee
     * @return the Employee entity if found
     */
    Optional<Employee> getEmployeeById(Long id);

    /**
     * Retrieves all Employees with pagination.
     *
     * @param pageable
     *            pagination information
     * @return paginated list of Employees
     */
    Page<Employee> getAllEmployees(Pageable pageable);

    /**
     * Retrieves all employees who are veterinarians.
     *
     * @return list of veterinarian employees
     */
    List<Employee> getVeterinarians();
}