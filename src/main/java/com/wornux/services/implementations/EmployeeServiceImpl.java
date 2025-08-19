package com.wornux.services.implementations;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.wornux.data.entity.Employee;
import com.wornux.data.entity.WorkScheduleDay;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.repository.EmployeeRepository;
import com.wornux.dto.request.EmployeeCreateRequestDto;
import com.wornux.dto.request.EmployeeUpdateRequestDto;
import com.wornux.dto.request.WorkScheduleDayDto;
import com.wornux.exception.*;
import com.wornux.mapper.EmployeeMapper;
import com.wornux.services.interfaces.EmployeeService;
import jakarta.validation.Valid;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ValidationException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

            // Validate work schedule
            validateWorkSchedule(value.getWorkScheduleDays());

            

            Employee employee = employeeMapper.toEntity(value);
            employee.setPassword(passwordEncoder.encode(value.getPassword()));
            // Ensure the collection is a new instance to avoid Hibernate/Envers issues
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
        return employeeRepository.findAll().stream()
            .filter(employee -> employee.getEmployeeRole() == EmployeeRole.VETERINARIAN)
            .toList();
    }

    @Transactional
    @Override
    public void updateEmployee(@NonNull Long id, @Valid EmployeeUpdateRequestDto employeeUpdateRequestDto) {
        log.debug("Request to update Employee with ID: {}", id);
        Employee employee = employeeRepository
            .findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));

        // Validar email único (excluyendo el empleado actual)
        if (employeeUpdateRequestDto.getEmail() != null &&
            !employeeUpdateRequestDto.getEmail().equals(employee.getEmail())) {
            employeeRepository.findByEmailAndIdNot(employeeUpdateRequestDto.getEmail(), id)
                .ifPresent(existing -> {
                    throw new ValidationException("El correo electrónico ya existe");
                });
        }

        // Validar username único (excluyendo el empleado actual)
        if (employeeUpdateRequestDto.getUsername() != null &&
            !employeeUpdateRequestDto.getUsername().equals(employee.getUsername())) {
            employeeRepository.findByUsernameAndIdNot(employeeUpdateRequestDto.getUsername(), id)
                .ifPresent(existing -> {
                    throw new DuplicateEmployeeException("username", employeeUpdateRequestDto.getUsername());
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

    /**
     * Validates work schedule for consistency and business rules
     */
    private void validateWorkSchedule(List<WorkScheduleDayDto> scheduleDays) {
        if (scheduleDays == null || scheduleDays.isEmpty()) {
            return; // Allow empty schedules
        }

        // Check for duplicate days
        Set<DayOfWeek> days = scheduleDays.stream()
            .map(WorkScheduleDayDto::getDayOfWeek)
            .collect(Collectors.toSet());

        if (days.size() != scheduleDays.size()) {
            throw new ValidationException("Duplicate days found in work schedule");
        }

        // Validate each day's time range
        for (WorkScheduleDayDto day : scheduleDays) {
            if (!day.isValidTimeRange()) {
                throw new ValidationException("Invalid time range for " + day.getDayOfWeek());
            }
        }
    }

    /**
     * Migrates legacy string schedule to structured format
     * This is a simple implementation - enhance based on your legacy format
     */
    private List<WorkScheduleDayDto> migrateLegacySchedule(String legacySchedule) {
        List<WorkScheduleDayDto> schedule = new ArrayList<>();

        if (legacySchedule == null || legacySchedule.trim().isEmpty()) {
            return schedule;
        }

        // Simple migration: if it contains "9-5" pattern, create Mon-Fri schedule
        if (legacySchedule.toLowerCase().contains("9") && legacySchedule.toLowerCase().contains("5")) {
            for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                schedule.add(WorkScheduleDayDto.builder()
                    .dayOfWeek(day)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .isOffDay(false)
                    .build());
            }

            // Add weekend off days
            for (DayOfWeek day : List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                schedule.add(WorkScheduleDayDto.builder()
                    .dayOfWeek(day)
                    .isOffDay(true)
                    .build());
            }
        }

        return schedule;
    }

    /**
     * Finds employees available on a specific day and time
     */
    public List<Employee> findEmployeesAvailableOnDayAndTime(DayOfWeek dayOfWeek, LocalTime time) {
        return employeeRepository.findEmployeesAvailableOnDayAndTime(dayOfWeek, time);
    }

    /**
     * Finds employees working on a specific day
     */
    public List<Employee> findEmployeesWorkingOnDay(DayOfWeek dayOfWeek) {
        return employeeRepository.findEmployeesWorkingOnDay(dayOfWeek);
    }

    @Override
    public double calculateWeeklyHours(Employee employee) {
        return employee.getWorkScheduleDays().stream()
            .filter(day -> !day.isOffDay())
            .filter(day -> day.getStartTime() != null && day.getEndTime() != null)
            .mapToDouble(day -> {
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
            .sorted((e1, e2) -> Double.compare(
                calculateWeeklyHours(e2),
                calculateWeeklyHours(e1)))
            .limit(limit)
            .collect(Collectors.toList());
    }

    public record ScheduleConflict(
            Employee employee,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            String reason
    ) {
    }

    public List<ScheduleConflict> detectScheduleConflicts(List<Employee> employees) {
        List<ScheduleConflict> conflicts = new ArrayList<>();

        for (Employee employee : employees) {
            for (WorkScheduleDay day : employee.getWorkScheduleDays()) {
                if (!day.isOffDay() && day.getStartTime() != null && day.getEndTime() != null) {
                    // Check for invalid time ranges
                    if (!day.isValidTimeRange()) {
                        conflicts.add(new ScheduleConflict(
                            employee,
                            day.getDayOfWeek(),
                            day.getStartTime(),
                            "Start time is after end time"
                        ));
                    }

                    // Check for very long shifts (>12 hours)
                    Duration duration = Duration.between(day.getStartTime(), day.getEndTime());
                    if (duration.toHours() > 12) {
                        conflicts.add(new ScheduleConflict(
                            employee,
                            day.getDayOfWeek(),
                            day.getStartTime(),
                            "Shift exceeds 12 hours"
                        ));
                    }
                }
            }

            // Check for employees with no working days
            boolean hasWorkingDays = employee.getWorkScheduleDays().stream()
                .anyMatch(day -> !day.isOffDay());

            if (!hasWorkingDays) {
                conflicts.add(new ScheduleConflict(
                    employee,
                    null,
                    null,
                    "Employee has no working days scheduled"
                ));
            }
        }

        return conflicts;
    }
}