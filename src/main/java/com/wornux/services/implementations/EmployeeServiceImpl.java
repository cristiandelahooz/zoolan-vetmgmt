package com.wornux.services.implementations;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.wornux.data.entity.Employee;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.repository.EmployeeRepository;
import com.wornux.dto.request.EmployeeCreateRequestDto;
import com.wornux.dto.request.EmployeeUpdateRequestDto;
import com.wornux.dto.request.WorkScheduleDayDto;
import com.wornux.exception.*;
import com.wornux.mapper.EmployeeMapper;
import com.wornux.services.interfaces.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed
public class EmployeeServiceImpl extends ListRepositoryService<Employee, Long, EmployeeRepository>
    implements EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final EmployeeMapper employeeMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Optional<Employee> getEmployeeById(Long id) {
    log.debug("Fetching employee with ID: {}", id);
    return employeeRepository.findById(id);
  }

  @Override
  public Page<Employee> getAllEmployees(Pageable pageable) {
    if (pageable == null || pageable.isUnpaged()) {
      List<Employee> all = employeeRepository.findAll();
      return new PageImpl<>(all);
    }
    return employeeRepository.findAll(pageable);
  }

  @Override
  public EmployeeCreateRequestDto save(@NonNull EmployeeCreateRequestDto value) {
    try {
      log.debug("Request to save Employee via FormService: {}", value);

      if (employeeRepository.existsByUsername(value.getUsername())) {
        throw new DuplicateEmployeeException("username", value.getUsername());
      }

      validateWorkSchedule(value.getWorkScheduleDays());

      Employee employee = employeeMapper.toEntity(value);
      employee.setPassword(passwordEncoder.encode(value.getPassword()));
      employee.setWorkScheduleDays(new ArrayList<>(employee.getWorkScheduleDays()));
      employee = employeeRepository.save(employee);

      EmployeeCreateRequestDto result = employeeMapper.toDTO(employee);
      log.info("Employee saved successfully via FormService with ID: {}", employee.getId());
      return result;
    } catch (Exception e) {
      log.error("Error saving Employee via FormService: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  public void delete(@NonNull Long id) {
    log.debug("Request to delete Employee via FormService: {}", id);

    Employee employee =
        employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

    employee.setActive(false);
    employee.setAvailable(false);
    employeeRepository.save(employee);

    log.info("Employee deactivated via FormService, ID: {}", id);
  }

  @Transactional(readOnly = true)
  public List<Employee> getVeterinarians() {
    log.debug("Request to get all veterinarians");
    return employeeRepository.findAvailableVeterinarians();
  }

  @Transactional
  public List<Employee> getEmployeesByRole(EmployeeRole role) {
    log.debug("Request to get all employees with role: {}", role);
    return employeeRepository.findAvailableEmployeesByRole(role);
  }

  @Transactional(readOnly = true)
  @Override
  public Page<Employee> getAllAvailableEmployees(Specification<Employee> spec, Pageable pageable) {
    log.debug("Request to get all available employees with filter and pagination");
    return employeeRepository.findAllAvailable(spec, pageable);
  }

  @Transactional
  @Override
  public void updateEmployee(
      @NonNull Long id, @Valid EmployeeUpdateRequestDto employeeUpdateRequestDto) {
    log.debug("Request to update Employee with ID: {}", id);
    Employee employee =
        employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

    // Validar email único (excluyendo el empleado actual)
    if (employeeUpdateRequestDto.getEmail() != null
        && !employeeUpdateRequestDto.getEmail().equals(employee.getEmail())) {
      employeeRepository
          .findByEmailAndIdNot(employeeUpdateRequestDto.getEmail(), id)
          .ifPresent(
              existing -> {
                throw new ValidationException("El correo electrónico ya existe");
              });
    }

    // Validar username único (excluyendo el empleado actual)
    if (employeeUpdateRequestDto.getUsername() != null
        && !employeeUpdateRequestDto.getUsername().equals(employee.getUsername())) {
      employeeRepository
          .findByUsernameAndIdNot(employeeUpdateRequestDto.getUsername(), id)
          .ifPresent(
              existing -> {
                throw new DuplicateEmployeeException(
                    "username", employeeUpdateRequestDto.getUsername());
              });
    }

    // Validate work schedule
    validateWorkSchedule(employeeUpdateRequestDto.getWorkScheduleDays());

    employeeMapper.updateEmployeeFromDto(employeeUpdateRequestDto, employee);
    // Ensure the collection is a new instance to avoid Hibernate/Envers issues
    employee.setWorkScheduleDays(new ArrayList<>(employee.getWorkScheduleDays()));
    employeeRepository.save(employee);
    log.info("Employee updated successfully with ID: {}", id);
  }

  @Override
  public EmployeeRepository getRepository() {
    return this.employeeRepository;
  }

  @Transactional(readOnly = true)
  public List<Employee> getGroomers() {
    log.debug("Request to get all groomers");
    return employeeRepository.findAll().stream()
        .filter(employee -> employee.getEmployeeRole() == EmployeeRole.GROOMER)
        .toList();
  }

  /** Validates work schedule for consistency and business rules */
  private void validateWorkSchedule(List<WorkScheduleDayDto> scheduleDays) {
    if (scheduleDays == null || scheduleDays.isEmpty()) {
      return;
    }

    Set<DayOfWeek> days =
        scheduleDays.stream().map(WorkScheduleDayDto::getDayOfWeek).collect(Collectors.toSet());

    if (days.size() != scheduleDays.size()) {
      throw new ValidationException("Duplicate days found in work schedule");
    }

    for (WorkScheduleDayDto day : scheduleDays) {
      if (!day.isValidTimeRange()) {
        throw new ValidationException("Invalid time range for " + day.getDayOfWeek());
      }
    }
  }

  /** Finds employees available on a specific day and time */
  public List<Employee> findEmployeesAvailableOnDayAndTime(DayOfWeek dayOfWeek, LocalTime time) {
    return employeeRepository.findEmployeesAvailableOnDayAndTime(dayOfWeek, time);
  }

  /** Finds employees working on a specific day */
  public List<Employee> findEmployeesWorkingOnDay(DayOfWeek dayOfWeek) {
    return employeeRepository.findEmployeesWorkingOnDay(dayOfWeek);
  }

  @Override
  public double calculateWeeklyHours(Employee employee) {
    return employee.getWorkScheduleDays().stream()
        .filter(day -> !day.isOffDay())
        .filter(day -> day.getStartTime() != null && day.getEndTime() != null)
        .mapToDouble(
            day -> {
              LocalTime start = day.getStartTime();
              LocalTime end = day.getEndTime();
              return Duration.between(start, end).toHours();
            })
        .sum();
  }

  @Override
  public List<Employee> findMostAvailableEmployees(int limit) {
    return employeeRepository.findAll().stream()
        .filter(Employee::isAvailable)
        .sorted((e1, e2) -> Double.compare(calculateWeeklyHours(e2), calculateWeeklyHours(e1)))
        .limit(limit)
        .collect(Collectors.toList());
  }
  
  public List<Employee> getAvailableVets() {
    return employeeRepository.findByEmployeeRoleAndAvailable(EmployeeRole.VETERINARIAN, true);
  }

  @Transactional
  public void markVetBusy(Long vetId) {
    Employee e = employeeRepository.findById(vetId)
            .orElseThrow(() -> new EntityNotFoundException("Vet not found: " + vetId));
    e.setAvailable(false);
    employeeRepository.save(e);
  }

  @Transactional
  public void markVetAvailable(Long vetId) {
    Employee e = employeeRepository.findById(vetId)
            .orElseThrow(() -> new EntityNotFoundException("Vet not found: " + vetId));
    e.setAvailable(true);
    employeeRepository.save(e);
  }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> findByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }
}
