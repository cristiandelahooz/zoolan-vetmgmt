package com.zoolandia.app.features.employee.service;

import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.zoolandia.app.features.employee.domain.Employee;
import com.zoolandia.app.features.employee.domain.EmployeeRole;
import com.zoolandia.app.features.employee.repository.EmployeeRepository;
import com.zoolandia.app.features.employee.service.dto.EmployeeCreateDTO;
import com.zoolandia.app.features.employee.service.dto.EmployeeUpdateDTO;
import com.zoolandia.app.features.employee.service.exception.*;
import com.zoolandia.app.features.employee.mapper.EmployeeMapper;
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
public class EmployeeServiceImpl extends ListRepositoryService<Employee, Long, EmployeeRepository> implements EmployeeService  {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Employee createEmployee(@Valid EmployeeCreateDTO employeeDTO) {
        log.debug("Creating new employee with username: {}", employeeDTO.getUsername());
        
        if (employeeRepository.existsByUsername(employeeDTO.getUsername())) {
            throw new DuplicateEmployeeException("username", employeeDTO.getUsername());
        }

        if (employeeDTO.getHireDate() != null && employeeDTO.getBirthDate() != null &&
            employeeDTO.getHireDate().isBefore(employeeDTO.getBirthDate())) {
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

        if (employeeDTO.getUsername() != null &&
            !existingEmployee.getUsername().equals(employeeDTO.getUsername()) &&
            employeeRepository.existsByUsername(employeeDTO.getUsername())) {
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

    @Override
    public Page<Employee> getAllEmployees(Pageable pageable) {
        log.debug("Fetching page {} of employees", pageable.getPageNumber());
        return employeeRepository.findAll(pageable);
    }

    @Override
    public Page<Employee> searchEmployees(String searchTerm, Pageable pageable) {
        log.debug("Searching employees with term: {}", searchTerm);
        return employeeRepository.findBySearchTerm(searchTerm, pageable);
    }

    @Override
    public Page<Employee> getEmployeesByRole(EmployeeRole role, Pageable pageable) {
        log.debug("Fetching employees with role: {}", role);
        return employeeRepository.findByEmployeeRole(role, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getAvailableVeterinarians() {
        return employeeRepository.findAvailableVeterinarians();
    }

    @Override
    public void updateEmployeeRole(Long id, EmployeeRole newRole) {
        log.debug("Updating role to {} for employee ID: {}", newRole, id);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
            
        validateRoleChange(employee.getEmployeeRole(), newRole);
        employee.setEmployeeRole(newRole);
        employeeRepository.save(employee);
    }

    @Override
    public void updateEmployeeSalary(Long id, Double newSalary) {
        log.debug("Updating salary for employee ID: {}", id);
        
        if (newSalary < 0) {
            throw new InvalidSalaryException(newSalary);
        }

        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
            
        employee.setSalary(newSalary);
        employeeRepository.save(employee);
    }

    @Override
    public void deactivateEmployee(Long id) {
        log.debug("Deactivating employee with ID: {}", id);
        updateEmployeeStatus(id, false);
    }

    @Override
    public void reactivateEmployee(Long id) {
        log.debug("Reactivating employee with ID: {}", id);
        updateEmployeeStatus(id, true);
    }

    @Override
    public void updateWorkSchedule(Long id, String workSchedule) {
        log.debug("Updating work schedule for employee ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
            
        employeeRepository.save(employee);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void setEmergencyAvailability(Long id, boolean available) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
        
        employee.setAvailable(available);
        employeeRepository.save(employee);
        
        log.info("Updated availability status to {} for employee ID: {}", available, id);
    }

    @Override
    public void addCertification(Long id, String certification) {
        log.debug("Adding certification for employee ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
            
        employeeRepository.save(employee);
    }

    @Override
    public Page<Employee> getEmployeesByHireDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Fetching employees hired between {} and {}", startDate, endDate);
        return employeeRepository.findByHireDateBetween(startDate, endDate, pageable);
    }

    @Override
    public void updateEmergencyContact(Long id, String contactName, String contactPhone) {
        log.debug("Updating emergency contact for employee ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
            
        employeeRepository.save(employee);
    }


    @Override
    public void deleteEmployee(Long id) {
        log.debug("Deleting employee with ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
            
        if (hasActiveAssignments(employee)) {
            throw new EmployeeOperationNotAllowedException("delete",
                "Employee has active assignments");
        }
        
        employeeRepository.delete(employee);
    }


    private void updateEmployeeStatus(Long id, boolean active) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
            
        employee.setActive(active);
        employeeRepository.save(employee);
    }

    private void validateRoleChange(EmployeeRole currentRole, EmployeeRole newRole) {
        if (isRoleDemoted(currentRole, newRole)) {
            throw new InvalidEmployeeRoleException(newRole.toString(),
                "Role demotion requires special authorization");
        }
    }

    private boolean isRoleDemoted(EmployeeRole currentRole, EmployeeRole newRole) {

        return false;
    }

    private boolean canHandleEmergencies(EmployeeRole role) {
        return role == EmployeeRole.VETERINARY_ASSISTANT ||
               role == EmployeeRole.LAB_TECHNICIAN;
    }

    private boolean hasActiveAssignments(Employee employee) {
        return false;
    }
}