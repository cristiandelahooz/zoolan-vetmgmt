package com.zoolandia.app.features.employee.service;

import com.vaadin.hilla.BrowserCallable;
import com.zoolandia.app.features.employee.domain.Employee;
import com.zoolandia.app.features.employee.domain.EmployeeRole;
import com.zoolandia.app.features.employee.service.dto.EmployeeCreateDTO;
import com.zoolandia.app.features.employee.service.dto.EmployeeUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
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
     * Creates a new Employee.
     *
     * @param employeeDTO the DTO containing the employee data
     * @return the created Employee entity
     */
    @PreAuthorize("hasRole('ADMIN')")
    Employee createEmployee(@Valid EmployeeCreateDTO employeeDTO);

    /**
     * Updates an existing Employee.
     *
     * @param id the ID of the employee to update
     * @param employeeDTO the DTO containing the updated employee data
     * @return the updated Employee entity
     */
    @PreAuthorize("hasRole('ADMIN')")
    Employee updateEmployee(Long id, @Valid EmployeeUpdateDTO employeeDTO);

    /**
     * Retrieves an Employee by ID.
     *
     * @param id the ID of the employee
     * @return the Employee entity if found
     */
    Optional<Employee> getEmployeeById(Long id);

    /**
     * Retrieves an Employee by username.
     *
     * @param username the username of the employee
     * @return the Employee entity if found
     */
    Optional<Employee> getEmployeeByUsername(String username);

    /**
     * Retrieves all Employees with pagination.
     *
     * @param pageable pagination information
     * @return paginated list of Employees
     */
    Page<Employee> getAllEmployees(Pageable pageable);

    /**
     * Searches for Employees based on a search term.
     *
     * @param searchTerm the term to search for in employee fields
     * @param pageable pagination information
     * @return paginated list of matching Employees
     */
    Page<Employee> searchEmployees(String searchTerm, Pageable pageable);

    /**
     * Retrieves Employees by role.
     *
     * @param employeeRole the employee role to filter by
     * @param pageable pagination information
     * @return paginated list of Employees with specified role
     */
    Page<Employee> getEmployeesByRole(EmployeeRole employeeRole, Pageable pageable);

    /**
     * Retrieves active veterinarians available for emergency services.
     *
     * @return list of available veterinarians
     */
    List<Employee> getAvailableVeterinarians();

    /**
     * Updates an Employee's role.
     *
     * @param id the ID of the employee
     * @param newRole the new role to set
     */
    @PreAuthorize("hasRole('ADMIN')")
    void updateEmployeeRole(Long id, EmployeeRole newRole);

    /**
     * Updates an Employee's salary.
     *
     * @param id the ID of the employee
     * @param newSalary the new salary to set
     */
    @PreAuthorize("hasRole('ADMIN')")
    void updateEmployeeSalary(Long id, Double newSalary);

    /**
     * Deactivates an Employee account.
     *
     * @param id the ID of the employee to deactivate
     */
    @PreAuthorize("hasRole('ADMIN')")
    void deactivateEmployee(Long id);

    /**
     * Reactivates a previously deactivated Employee account.
     *
     * @param id the ID of the employee to reactivate
     */
    @PreAuthorize("hasRole('ADMIN')")
    void reactivateEmployee(Long id);

    /**
     * Updates an Employee's work schedule.
     *
     * @param id the ID of the employee
     * @param workSchedule the new work schedule
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    void updateWorkSchedule(Long id, String workSchedule);

    /**
     * Sets an Employee's emergency availability status.
     *
     * @param id the ID of the employee
     * @param available whether the employee is available for emergencies
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    void setEmergencyAvailability(Long id, boolean available);

    /**
     * Adds a certification or specialization to an Employee.
     *
     * @param id the ID of the employee
     * @param certification the certification to add
     */
    @PreAuthorize("hasRole('ADMIN')")
    void addCertification(Long id, String certification);

    /**
     * Retrieves Employees hired between specific dates.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return paginated list of Employees hired between the specified dates
     */
    Page<Employee> getEmployeesByHireDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Updates emergency contact information for an Employee.
     *
     * @param id the ID of the employee
     * @param contactName emergency contact name
     * @param contactPhone emergency contact phone number
     */
    void updateEmergencyContact(Long id, String contactName, String contactPhone);

    /**
     * Permanently deletes an Employee.
     * Should only be used in specific administrative cases.
     *
     * @param id the ID of the employee to delete
     */
    @PreAuthorize("hasRole('ADMIN')")
    void deleteEmployee(Long id);
}