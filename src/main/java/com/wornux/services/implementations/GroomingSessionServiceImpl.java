package com.wornux.services.implementations;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.wornux.data.entity.Employee;
import com.wornux.data.entity.GroomingSession;
import com.wornux.data.entity.Pet;
import com.wornux.data.repository.EmployeeRepository;
import com.wornux.data.repository.GroomingSessionRepository;
import com.wornux.data.repository.PetRepository;
import com.wornux.data.repository.WaitingRoomRepository;
import com.wornux.dto.request.CreateGroomingSessionRequestDto;
import com.wornux.dto.request.UpdateGroomingSessionRequestDto;
import com.wornux.exception.EmployeeNotFoundException;
import com.wornux.exception.PetNotFoundException;
import com.wornux.mapper.GroomingSessionMapper;
import com.wornux.services.interfaces.EmployeeService;
import com.wornux.services.interfaces.GroomingSessionService;
import com.wornux.services.interfaces.PetService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wornux.data.entity.WaitingRoom;
import com.wornux.data.enums.WaitingRoomStatus;
import com.wornux.data.enums.EmployeeRole;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed
public class GroomingSessionServiceImpl
    extends ListRepositoryService<GroomingSession, Long, GroomingSessionRepository>
    implements GroomingSessionService {

  private final GroomingSessionRepository groomingSessionRepository;
  private final PetService petService;
  private final EmployeeService employeeService;
  private final PetRepository petRepository;
  private final EmployeeRepository employeeRepository;
  private final GroomingSessionMapper groomingSessionMapper;
  private final WaitingRoomRepository waitingRoomRepository;

  @Override
  public GroomingSession save(GroomingSession session) {
    log.debug("Saving GroomingSession: {}", session.getId());
    return groomingSessionRepository.save(session);
  }

  @Override
  public GroomingSession create(CreateGroomingSessionRequestDto createDTO) {
    log.debug("Request to create GroomingSession : {}", createDTO);

    Pet pet =
        petRepository
            .findById(createDTO.getPetId())
            .orElseThrow(() -> new PetNotFoundException(createDTO.getPetId()));

    Employee groomer = null;
    if (createDTO.getGroomerId() != null) {
      groomer =
          employeeRepository
              .findById(createDTO.getGroomerId())
              .orElseThrow(() -> new EmployeeNotFoundException(createDTO.getGroomerId()));
    }

    GroomingSession session = groomingSessionMapper.toEntity(createDTO);
    session.setPet(pet);
    session.setGroomer(groomer);
    if (session.getGroomingDate() == null) {
      session.setGroomingDate(LocalDateTime.now());
    }

    GroomingSession saved = groomingSessionRepository.save(session);
    log.info("Created GroomingSession with ID: {} for Pet: {}", saved.getId(), pet.getId());
    return saved;
  }

  @Override
  public GroomingSession update(Long id, UpdateGroomingSessionRequestDto updateDTO) {
    log.debug("Request to update GroomingSession : {}", updateDTO);

    GroomingSession session = findById(id);

    Pet pet =
        petService
            .getPetById(updateDTO.getPetId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Pet not found with id: " + updateDTO.getPetId()));

    Employee groomer = null;
    if (updateDTO.getGroomerId() != null) {
      groomer =
          employeeService
              .getEmployeeById(updateDTO.getGroomerId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Employee not found with id: " + updateDTO.getGroomerId()));
    }

    if (updateDTO.getNotes() != null) {
      session.setNotes(updateDTO.getNotes());
    }

    if (updateDTO.getGroomingDate() != null) {
      session.setGroomingDate(updateDTO.getGroomingDate());
    }

    session.setPet(pet);
    session.setGroomer(groomer);
    session.setUpdatedAt(LocalDateTime.now());

    return groomingSessionRepository.save(session);
  }

  @Override
  public GroomingSession partialUpdate(Long id, UpdateGroomingSessionRequestDto updateDTO) {
    log.debug("Request to partially update GroomingSession : {}", updateDTO);

    GroomingSession session = findById(id);

    if (updateDTO.getNotes() != null) {
      session.setNotes(updateDTO.getNotes());
    }

    if (updateDTO.getGroomingDate() != null) {
      session.setGroomingDate(updateDTO.getGroomingDate());
    }

    if (updateDTO.getPetId() != null) {
      Pet pet =
          petService
              .getPetById(updateDTO.getPetId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Pet not found with id: " + updateDTO.getPetId()));
      session.setPet(pet);
    }

    if (updateDTO.getGroomerId() != null) {
      Employee groomer =
          employeeService
              .getEmployeeById(updateDTO.getGroomerId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Employee not found with id: " + updateDTO.getGroomerId()));
      session.setGroomer(groomer);
    }

    session.setUpdatedAt(LocalDateTime.now());
    return groomingSessionRepository.save(session);
  }

  @Override
  public GroomingSession findById(Long id) {
    log.debug("Request to get GroomingSession : {}", id);
    return groomingSessionRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("GroomingSession not found with id: " + id));
  }

  @Override
  public Page<GroomingSession> findAll(Pageable pageable) {
    return groomingSessionRepository.findByActiveTrue(pageable);
  }

  @Override
  public List<GroomingSession> findByPetId(Long petId) {
    return groomingSessionRepository.findByPetIdAndActiveTrue(petId);
  }

  @Override
  public List<GroomingSession> findByGroomerId(Long groomerId) {
    return groomingSessionRepository.findByGroomerIdAndActiveTrue(groomerId);
  }

  @Override
  public void delete(Long id) {
    log.debug("Request to deactivate GroomingSession : {}", id);
    GroomingSession session = findById(id);
    session.setActive(false);
    session.setUpdatedAt(LocalDateTime.now());
    groomingSessionRepository.save(session);
  }

  @Override
  public Page<GroomingSession> findByActiveTrue(Pageable pageable) {
    log.debug("Request to get all active GroomingSessions");
    return groomingSessionRepository.findByActiveTrue(pageable);
  }

  @Override
  public GroomingSessionRepository getRepository() {
    return groomingSessionRepository;
  }

  @Override
  @Transactional
  public void assignFromWaitingRoom(Long waitingRoomId, Long groomerId) {
    WaitingRoom wr = waitingRoomRepository.findById(waitingRoomId)
            .orElseThrow(() -> new EntityNotFoundException("WaitingRoom no encontrado: " + waitingRoomId));

    if (wr.getStatus() != WaitingRoomStatus.ESPERANDO) {
      throw new IllegalStateException("Solo se puede asignar un groomer cuando el estado es ESPERANDO.");
    }

    Employee groomer = employeeRepository.findById(groomerId)
            .orElseThrow(() -> new EntityNotFoundException("Groomer no encontrado: " + groomerId));

    if (groomer.getEmployeeRole() != EmployeeRole.GROOMER) {
      throw new IllegalStateException("El empleado seleccionado no es groomer.");
    }
    if (!groomer.isAvailable()) {
      throw new IllegalStateException("El groomer seleccionado no está disponible.");
    }

    // marcar ocupado y guardar
    groomer.setAvailable(false);
    employeeRepository.save(groomer);

    wr.setAssignedGroomer(groomer);
    waitingRoomRepository.save(wr);

    log.info("Groomer {} asignado a WaitingRoom {}", groomer.getId(), wr.getId());
  }

  @Override
  @Transactional
  public GroomingSession start(Long waitingRoomId) {
    WaitingRoom wr = waitingRoomRepository.findById(waitingRoomId)
            .orElseThrow(() -> new EntityNotFoundException("WaitingRoom no encontrado: " + waitingRoomId));

    if (wr.getStatus() != WaitingRoomStatus.ESPERANDO) {
      throw new IllegalStateException("Solo se puede iniciar si el estado es ESPERANDO.");
    }
    if (wr.getAssignedGroomer() == null) {
      throw new IllegalStateException("No hay groomer asignado a esta entrada.");
    }

    // mover WR a EN_PROCESO
    wr.startService();
    waitingRoomRepository.save(wr);

    // crear sesión
    GroomingSession s = new GroomingSession();
    s.setPet(wr.getPet());
    s.setGroomer(wr.getAssignedGroomer());
    s.setGroomingDate(LocalDateTime.now());
    s.setActive(true);
    // s.setStatus(GroomingStatus.EN_PROCESO); // opcional si tienes enum/estado

    // si tu entidad tiene relación con WR, guárdala:
    // s.setWaitingRoom(wr); // opcional

    GroomingSession saved = groomingSessionRepository.save(s);
    return saved;
  }

  @Override
  @Transactional
  public void finish(Long groomingSessionId) {
    GroomingSession s = groomingSessionRepository.findById(groomingSessionId)
            .orElseThrow(() -> new EntityNotFoundException("GroomingSession not found: " + groomingSessionId));

    // Marcar sesión como finalizada (si tienes campos de estado/fin)
    // s.setStatus(GroomingStatus.COMPLETADO); // opcional
    // s.setFinishedAt(LocalDateTime.now());   // opcional
    groomingSessionRepository.save(s);

    // Cerrar WaitingRoom asociado (si no hay relación directa, busca por mascota + estado activo)
    WaitingRoom wr = null;
    // si tu entidad tiene getWaitingRoom():
    // wr = s.getWaitingRoom();
    if (wr == null) {
      wr = waitingRoomRepository.findTopByPet_IdAndStatusInOrderByArrivalTimeDesc(
              s.getPet().getId(),
              List.of(WaitingRoomStatus.EN_PROCESO, WaitingRoomStatus.ESPERANDO)
      ).orElse(null);
    }
    if (wr != null) {
      wr.completeService();
      waitingRoomRepository.save(wr);
    }

    // Liberar groomer
    Employee groomer = s.getGroomer();
    if (groomer != null) {
      groomer.setAvailable(true);
      employeeRepository.save(groomer);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<GroomingSession> findForGroomer(Long groomerId) {
    // Reutiliza tu query existente ordenando si lo necesitas
    return groomingSessionRepository.findByGroomerIdAndActiveTrue(groomerId);
  }

}
