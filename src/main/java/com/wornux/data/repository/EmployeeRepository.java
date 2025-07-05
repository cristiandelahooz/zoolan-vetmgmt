package com.wornux.data.repository;

import com.wornux.data.entity.Employee;
import com.wornux.data.enums.EmployeeRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link Employee} entity.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    /**
     * Finds an employee by their username.
     *
     * @param username
     *            the username to search for
     * @return the employee if found
     */
    Optional<Employee> findByUsername(String username);

    /**
     * Checks if an employee exists with the given username.
     *
     * @param username
     *            the username to check
     * @return true if an employee exists with the username
     */
    boolean existsByUsername(String username);

    /**
     * Finds all employees with a specific role.
     *
     * @param employeeRole
     *            the role to filter by
     * @param pageable
     *            pagination information
     * @return page of employees with the specified role
     */
    Page<Employee> findByEmployeeRole(EmployeeRole employeeRole, Pageable pageable);

    /**
     * Finds employees hired between two dates.
     *
     * @param startDate
     *            start of the date range
     * @param endDate
     *            end of the date range
     * @param pageable
     *            pagination information
     * @return page of employees hired within the date range
     */
    Page<Employee> findByHireDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Searches for employees based on various fields.
     *
     * @param searchTerm
     *            the term to search for
     * @param pageable
     *            pagination information
     * @return page of employees matching the search term
     */
    @Query("SELECT e FROM Employee e WHERE " + "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(e.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(e.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Employee> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Finds all available veterinarians for emergency services.
     *
     * @return list of available veterinarians
     */
    @Query("SELECT e FROM Employee e WHERE e.employeeRole = 'VETERINARIAN' "
            + "AND e.active = true AND e.available = true")
    List<Employee> findAvailableVeterinarians();

    /**
     * Finds all employees by salary range.
     *
     * @param minSalary
     *            minimum salary
     * @param maxSalary
     *            maximum salary
     * @param pageable
     *            pagination information
     * @return page of employees within the salary range
     */
    Page<Employee> findBySalaryBetween(Double minSalary, Double maxSalary, Pageable pageable);

    /**
     * Counts active employees by role.
     *
     * @param employeeRole
     *            the role to count
     * @return number of active employees with the specified role
     */
    long countByEmployeeRoleAndAvailableTrue(EmployeeRole employeeRole);

    /**
     * Encuentra todos los empleados disponibles con un rol específico
     */
    @Query("SELECT e FROM Employee e WHERE e.employeeRole = :role AND e.available = true AND e.active = true")
    List<Employee> findAvailableEmployeesByRole(@Param("employeeRole") EmployeeRole employeeRole);

}