package com.wornux.data.repository;

import com.wornux.data.entity.Employee;
import com.wornux.data.enums.EmployeeRole;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for the {@link Employee} entity. */
@Repository
public interface EmployeeRepository
    extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

  /** Finds an employee by their username. */
  Optional<Employee> findByUsername(String username);

  /** Checks if an employee exists with the given username. */
  boolean existsByUsername(String username);

  /** Finds all employees with a specific role. */
  Page<Employee> findByEmployeeRole(EmployeeRole employeeRole, Pageable pageable);

  /** Finds employees hired between two dates. */
  Page<Employee> findByHireDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

  /** Searches for employees based on various fields. */
  @Query(
      "SELECT e FROM Employee e WHERE "
          + "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(e.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(e.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<Employee> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

  /** Finds all available veterinarians for emergency services. */
  @Query(
      "SELECT e FROM Employee e WHERE e.employeeRole = 'VETERINARIAN' "
          + "AND e.active = true AND e.available = true")
  List<Employee> findAvailableVeterinarians();

  /** Finds all employees with availability. */
  @Query("SELECT e FROM Employee e WHERE e.available = true")
  Page<Employee> findAllAvailable(Specification<Employee> spec, Pageable pageable);

  /** Finds all employees by salary range. */
  Page<Employee> findBySalaryBetween(Double minSalary, Double maxSalary, Pageable pageable);

  /** Counts active employees by role. */
  long countByEmployeeRoleAndAvailableTrue(EmployeeRole employeeRole);

  /** Encuentra todos los empleados disponibles con un rol específico */
  @Query(
      "SELECT e FROM Employee e WHERE e.employeeRole = :role AND e.available = true AND e.active = true")
  List<Employee> findAvailableEmployeesByRole(@Param("employeeRole") EmployeeRole employeeRole);

  Optional<Employee> findByEmail(String email);

  Optional<Employee> findByEmailAndIdNot(String email, Long id);

  Optional<Employee> findByUsernameAndIdNot(String username, Long id);

  /** Finds employees available on a specific day of week and time range */
  @Query(
      "SELECT DISTINCT e FROM Employee e JOIN e.workScheduleDays ws "
          + "WHERE ws.dayOfWeek = :dayOfWeek "
          + "AND ws.isOffDay = false "
          + "AND ws.startTime <= :time "
          + "AND ws.endTime >= :time "
          + "AND e.active = true AND e.available = true")
  List<Employee> findEmployeesAvailableOnDayAndTime(
      @Param("dayOfWeek") DayOfWeek dayOfWeek, @Param("time") LocalTime time);

  /** Finds employees working on a specific day */
  @Query(
      "SELECT DISTINCT e FROM Employee e JOIN e.workScheduleDays ws "
          + "WHERE ws.dayOfWeek = :dayOfWeek "
          + "AND ws.isOffDay = false "
          + "AND e.active = true")
  List<Employee> findEmployeesWorkingOnDay(@Param("dayOfWeek") DayOfWeek dayOfWeek);

  List<Employee> findByEmployeeRoleAndAvailable(EmployeeRole role, boolean available);
  
}
