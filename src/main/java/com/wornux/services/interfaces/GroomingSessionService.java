package com.wornux.services.interfaces;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.data.entity.GroomingSession;
import com.wornux.data.repository.GroomingSessionRepository;
import com.wornux.dto.request.CreateGroomingSessionRequestDto;
import com.wornux.dto.request.UpdateGroomingSessionRequestDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@BrowserCallable
@Validated
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface GroomingSessionService {

  /**
   * Create a new grooming session
   *
   * @param createDTO the grooming session data
   * @return the created grooming session
   */
  GroomingSession create(CreateGroomingSessionRequestDto createDTO);

  /**
   * Save a grooming session
   *
   * @param session the grooming session to save
   * @return the saved grooming session
   */
  GroomingSession save(GroomingSession session);

  /**
   * Update an existing grooming session
   *
   * @param id the id of the grooming session to update
   * @param updateDTO the updated grooming session data
   * @return the updated grooming session
   */
  GroomingSession update(Long id, UpdateGroomingSessionRequestDto updateDTO);

  /**
   * Partially update a grooming session
   *
   * @param id the id of the grooming session to update
   * @param updateDTO the fields to update
   * @return the updated grooming session
   */
  GroomingSession partialUpdate(Long id, UpdateGroomingSessionRequestDto updateDTO);

  /**
   * Get a grooming session by id
   *
   * @param id the grooming session id
   * @return the found grooming session
   */
  GroomingSession findById(Long id);

  /**
   * Get all grooming sessions with pagination
   *
   * @param pageable pagination information
   * @return page of grooming sessions
   */
  Page<GroomingSession> findAll(Pageable pageable);

  /**
   * Get all grooming sessions for a specific pet
   *
   * @param petId the pet id
   * @return list of grooming sessions
   */
  List<GroomingSession> findByPetId(Long petId);

  /**
   * Get all grooming sessions for a specific groomer
   *
   * @param groomerId the groomer id
   * @return list of grooming sessions
   */
  List<GroomingSession> findByGroomerId(Long groomerId);

  /**
   * Delete a grooming session
   *
   * @param id the grooming session id to delete
   */
  void delete(Long id);

  /**
   * Get all active grooming sessions with pagination
   *
   * @param pageable pagination information
   * @return page of active grooming sessions
   */
  Page<GroomingSession> findByActiveTrue(Pageable pageable);

  /**
   * Get the GroomingSession repository for direct database operations.
   *
   * @return the GroomingSessionRepository instance
   */
  GroomingSessionRepository getRepository();

  // Asignar un groomer desde la sala de espera (solo si está ESPERANDO)
  void assignFromWaitingRoom(Long waitingRoomId, Long groomerId);

  // Iniciar sesión de grooming desde la sala de espera (mueve WR a EN_PROCESO y crea la sesión)
  GroomingSession start(Long waitingRoomId);

  // Finalizar la sesión (marca WR como COMPLETADO y libera groomer)
  void finish(Long groomingSessionId);

  // Listar sesiones activas del groomer (similar a findForVeterinarian)
  List<GroomingSession> findForGroomer(Long groomerId);
}
