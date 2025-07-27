package com.wornux.services.implementations;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.FormService;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.wornux.data.entity.Employee;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.repository.EmployeeRepository;
import com.wornux.dto.request.EmployeeCreateRequestDto;
import com.wornux.dto.request.EmployeeUpdateRequestDto;
import com.wornux.exception.*;
import com.wornux.mapper.EmployeeMapper;
import com.wornux.services.interfaces.EmployeeService;
import jakarta.validation.Valid;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.springframework.data.repository.support.Repositories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed
public class EmployeeServiceImpl extends ListRepositoryService<Employee, Long, EmployeeRepository>
        implements EmployeeService, FormService<EmployeeCreateRequestDto, Long> {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Updates an existing employee. This method can be accessed by any authenticated user.
     *
     * @param dto
     *            the DTO containing employee data to update
     * @return the updated employee DTO
     */
    public EmployeeUpdateRequestDto update(@Valid EmployeeUpdateRequestDto dto) {
        if (dto.getId() == null) {
            throw new EmployeeNotFoundException("Employee ID is required for update");
        }
        Employee employee = employeeRepository.findById(dto.getId())
                .orElseThrow(() -> new EmployeeNotFoundException(dto.getId()));

        employeeMapper.updateEmployeeFromDto(dto, employee);
        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toUpdateDTO(updated);
    }

    @Override
    public Optional<Employee> getEmployeeById(Long id) {
        log.debug("Fetching employee with ID: {}", id);
        return employeeRepository.findById(id);
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
        if (pageable == null || pageable.isUnpaged()) {
            List<Employee> all = employeeRepository.findAll();
            return new PageImpl<>(all);
        }
        return employeeRepository.findAll(pageable);
    }

    /**
     * Implementation of FormService.save() method. This method is used by Vaadin Hilla for CRUD operations.
     */
    @Override
    public EmployeeCreateRequestDto save(@NonNull EmployeeCreateRequestDto value) {
        try {
            log.debug("Request to save Employee via FormService: {}", value);

            if (employeeRepository.existsByUsername(value.getUsername())) {
                throw new DuplicateEmployeeException("username", value.getUsername());
            }

            Employee employee = employeeMapper.toEntity(value);
            employee.setPassword(passwordEncoder.encode(value.getPassword()));
            employee = employeeRepository.save(employee);

            EmployeeCreateRequestDto result = employeeMapper.toDTO(employee);
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
    public void delete(@NonNull Long id) {
        log.debug("Request to delete Employee via FormService: {}", id);

        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

        employee.setActive(false);
        employee.setAvailable(false);
        employeeRepository.save(employee);

        log.info("Employee deactivated via FormService, ID: {}", id);

    }

    @Transactional(readOnly = true)
    public List<Employee> getVeterinarians() {
        log.debug("Request to get all veterinarians");
        return employeeRepository.findAll().stream()
                .filter(employee -> employee.getEmployeeRole() == EmployeeRole.VETERINARIAN).toList();
    }

    /*

     */
    @Override
    public EmployeeRepository getRepository(){
        return this.employeeRepository;
    }
}