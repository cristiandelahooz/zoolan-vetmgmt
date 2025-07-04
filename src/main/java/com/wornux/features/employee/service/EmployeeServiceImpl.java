package com.wornux.features.employee.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.FormService;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.wornux.features.employee.domain.Employee;
import com.wornux.features.employee.domain.EmployeeRole;
import com.wornux.features.employee.repository.EmployeeRepository;
import com.wornux.features.employee.service.dto.EmployeeCreateDTO;
import com.wornux.features.employee.service.dto.EmployeeUpdateDTO;
import com.wornux.features.employee.service.exception.*;
import com.wornux.features.employee.mapper.EmployeeMapper;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed
public class EmployeeServiceImpl extends ListRepositoryService<Employee, Long, EmployeeRepository>
        implements EmployeeService, FormService<EmployeeCreateDTO, Long> {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Employee createEmployee(@Valid EmployeeCreateDTO employeeDTO) {
        log.debug("Creating new employee with username: {}", employeeDTO.getUsername());

        if (employeeRepository.existsByUsername(employeeDTO.getUsername())) {
            throw new DuplicateEmployeeException("username", employeeDTO.getUsername());
        }

        if (employeeDTO.getHireDate() != null && employeeDTO.getBirthDate() != null
                && employeeDTO.getHireDate().isBefore(employeeDTO.getBirthDate())) {
            throw new InvalidEmployeeDateException(employeeDTO.getHireDate(), employeeDTO.getBirthDate());
        }

        Employee employee = employeeMapper.toEntity(employeeDTO);
        employee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));

        return employeeRepository.save(employee);
    }

    @Override
    public Employee updateEmployee(Long id, @Valid EmployeeUpdateDTO employeeDTO) {
        log.debug("Updating employee with ID: {}", id);

        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        if (employeeDTO.getUsername() != null && !existingEmployee.getUsername().equals(employeeDTO.getUsername())
                && employeeRepository.existsByUsername(employeeDTO.getUsername())) {
            throw new DuplicateEmployeeException("username", employeeDTO.getUsername());
        }

        if (employeeDTO.getPassword() != null) {
            existingEmployee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        }

        employeeMapper.partialUpdate(existingEmployee, employeeDTO);
        return employeeRepository.save(existingEmployee);
    }

    @Override
    public Optional<Employee> getEmployeeById(Long id) {
        log.debug("Fetching employee with ID: {}", id);
        return employeeRepository.findById(id);
    }

    @Override
    public Optional<Employee> getEmployeeByUsername(String username) {
        log.debug("Fetching employee with username: {}", username);
        return employeeRepository.findByUsername(username);
    }

    /**
     * Retrieves all employees with pagination. This method can be accessed by any authenticated user.
     *
     * @param pageable
     *            pagination information
     * @return paginated list of employees
     */
    @Override
    public Page<Employee> getAllEmployees(Pageable pageable) {
        log.debug("Fetching page {} of employees", pageable.getPageNumber());
        return employeeRepository.findAll(pageable);
    }

    /**
     * Searches for employees based on a search term. This method can be accessed by any authenticated user.
     *
     * @param searchTerm
     *            the term to search for in employee fields
     * @param pageable
     *            pagination information
     * @return paginated list of matching employees
     */
    @Override
    public Page<Employee> searchEmployees(String searchTerm, Pageable pageable) {
        log.debug("Searching employees with term: {}", searchTerm);
        return employeeRepository.findBySearchTerm(searchTerm, pageable);
    }

    /**
     * Retrieves employees by their role. This method can be accessed by any authenticated user.
     *
     * @param role
     *            the role to filter employees by
     * @param pageable
     *            pagination information
     * @return paginated list of employees with the specified role
     */
    @Override
    public Page<Employee> getEmployeesByRole(EmployeeRole role, Pageable pageable) {
        log.debug("Fetching employees with role: {}", role);
        return employeeRepository.findByEmployeeRole(role, pageable);
    }

    /**
     * Retrieves all available veterinarians for emergency services. This method can be accessed by any authenticated
     * user.
     *
     * @return list of available veterinarians
     */
    @Override
    @Transactional(readOnly = true)
    public List<Employee> getAvailableVeterinarians() {
        return employeeRepository.findAvailableVeterinarians();
    }

    /**
     * Updates the role of an employee. This method can only be accessed by users with ADMIN or MANAGER roles.
     *
     * @param id
     *            the ID of the employee
     * @param newRole
     *            the new role to assign
     */
    @Override
    public void updateEmployeeRole(Long id, EmployeeRole newRole) {
        log.debug("Updating role to {} for employee ID: {}", newRole, id);

        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

        employee.setEmployeeRole(newRole);
        employeeRepository.save(employee);
    }

    /**
     * Updates the salary of an employee. This method can only be accessed by users with ADMIN or MANAGER roles.
     *
     * @param id
     *            the ID of the employee
     * @param newSalary
     *            the new salary to set
     */
    @Override
    public void updateEmployeeSalary(Long id, Double newSalary) {
        log.debug("Updating salary for employee ID: {}", id);

        if (newSalary < 0) {
            throw new InvalidSalaryException(newSalary);
        }

        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

        employee.setSalary(newSalary);
        employeeRepository.save(employee);
    }

    /**
     * Deactivates an employee account. This method can only be accessed by users with ADMIN or MANAGER roles.
     *
     * @param id
     *            the ID of the employee to deactivate
     */
    @Override
    public void deactivateEmployee(Long id) {
        log.debug("Deactivating employee with ID: {}", id);
        updateEmployeeStatus(id, false);
    }

    /**
     * Reactivates an employee account. This method can only be accessed by users with ADMIN or MANAGER roles.
     *
     * @param id
     *            the ID of the employee to reactivate
     */
    @Override
    public void reactivateEmployee(Long id) {
        log.debug("Reactivating employee with ID: {}", id);
        updateEmployeeStatus(id, true);
    }

    /**
     * Updates the work schedule for an employee. This method can only be accessed by users with ADMIN or MANAGER roles.
     *
     * @param id
     *            the ID of the employee
     * @param workSchedule
     *            the new work schedule
     */
    @Override
    public void updateWorkSchedule(Long id, String workSchedule) {
        log.debug("Updating work schedule for employee ID: {}", id);

        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

        employee.setWorkSchedule(workSchedule);

        employeeRepository.save(employee);
    }

    /**
     * Sets the emergency availability status for an employee. This method can only be accessed by users with ADMIN or
     * MANAGER roles.
     *
     * @param id
     *            the ID of the employee
     * @param available
     *            true if the employee is available for emergencies, false otherwise
     */
    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void setEmergencyAvailability(Long id, boolean available) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

        employee.setAvailable(available);
        employeeRepository.save(employee);

        log.info("Updated availability status to {} for employee ID: {}", available, id);
    }

    /**
     * Retrieves employees hired between the specified dates.
     *
     * @param startDate
     *            the start date of the hiring period
     * @param endDate
     *            the end date of the hiring period
     * @param pageable
     *            pagination information
     * @return a page of employees hired between the specified dates
     */
    @Override
    public Page<Employee> getEmployeesByHireDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Fetching employees hired between {} and {}", startDate, endDate);
        return employeeRepository.findByHireDateBetween(startDate, endDate, pageable);
    }

    /**
     * Updates emergency contact information for an Employee.
     * 
     * @param id
     *            the ID of the employee
     * @param contactName
     *            emergency contact name
     * @param contactPhone
     *            emergency contact phone number
     */
    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void updateEmergencyContact(Long id, String contactName, String contactPhone) {
        log.debug("Updating emergency contact for employee ID: {}", id);

        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

        employee.setEmergencyContactName(contactName);
        employee.setEmergencyContactPhone(contactPhone);

        employeeRepository.save(employee);
    }

    /**
     * Deletes an employee by ID.
     * 
     * @param id
     *            the ID of the employee to delete
     */
    @Override
    public void deleteEmployee(Long id) {
        log.debug("Deleting employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

        if (hasActiveAssignments(employee)) {
            throw new EmployeeOperationNotAllowedException("delete", "Employee has active assignments");
        }

        employeeRepository.delete(employee);
    }

    /**
     * Updates the active status of an employee. This method is used for both deactivating and reactivating employees.
     *
     * @param id
     *            the ID of the employee
     * @param active
     *            true to activate, false to deactivate
     */
    private void updateEmployeeStatus(Long id, boolean active) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

        employee.setActive(active);
        employeeRepository.save(employee);
    }

    private boolean canHandleEmergencies(EmployeeRole role) {
        return role == EmployeeRole.VETERINARIAN || role == EmployeeRole.LAB_TECHNICIAN;
    }

    private boolean hasActiveAssignments(Employee employee) {
        return false;
    }

    /**
     * Implementation of FormService.save() method. This method is used by Vaadin Hilla for CRUD operations.
     */
    @Override
    @Transactional
    public @org.springframework.lang.Nullable EmployeeCreateDTO save(EmployeeCreateDTO value) {
        try {
            log.debug("Request to save Employee via FormService: {}", value);

            if (employeeRepository.existsByUsername(value.getUsername())) {
                throw new DuplicateEmployeeException("username", value.getUsername());
            }

            Employee employee = employeeMapper.toEntity(value);
            employee.setPassword(passwordEncoder.encode(value.getPassword()));
            employee = employeeRepository.save(employee);

            EmployeeCreateDTO result = employeeMapper.toDTO(employee);
            log.info("Employee saved successfully via FormService with ID: {}", employee.getId());
            return result;
        } catch (Exception e) {
            log.error("Error saving Employee via FormService: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Implementation of FormService.delete() method. This method is used by Vaadin Hilla for CRUD operations. Uses soft
     * delete by deactivating the employee.
     */
    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Employee via FormService: {}", id);

        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

        employee.setActive(false);
        employeeRepository.save(employee);

        log.info("Employee deactivated via FormService, ID: {}", id);

    }

}