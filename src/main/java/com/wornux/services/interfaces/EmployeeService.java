package com.wornux.services.interfaces;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.data.entity.Employee;
import com.wornux.data.repository.EmployeeRepository;
import com.wornux.dto.request.EmployeeCreateRequestDto;
import com.wornux.dto.request.EmployeeUpdateRequestDto;
import jakarta.validation.Valid;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/** Offering Interface for managing {@link Employee} entities. */
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
   * Retrieves all available Employees based on specifications and pagination.
   *
   * @param spec the specification to filter employees
   * @param pageable pagination information
   * @return paginated list of available Employees
   */
  Page<Employee> getAllAvailableEmployees(Specification<Employee> spec, Pageable pageable);

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

  /** Finds employees available on a specific day and time */
  List<Employee> findEmployeesAvailableOnDayAndTime(DayOfWeek dayOfWeek, LocalTime time);

  /** Finds employees working on a specific day */
  List<Employee> findEmployeesWorkingOnDay(DayOfWeek dayOfWeek);

  /** Gets total working hours for an employee per week */
  double calculateWeeklyHours(Employee employee);

  /** Finds employees with the most availability */
  List<Employee> findMostAvailableEmployees(int limit);

  void delete(@NonNull Long id);

  void updateEmployee(@NonNull Long id, @Valid EmployeeUpdateRequestDto dto);

  List<Employee> getGroomers();

  public List<Employee> getAvailableVets();

  public void markVetBusy(Long vetId);

  public void markVetAvailable(Long vetId);

  Optional<Employee> findByUsername(String username);

  public List<Employee> getAvailableGroomers();
}
