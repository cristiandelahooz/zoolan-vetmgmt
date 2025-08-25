package com.wornux.services.implementations;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.wornux.data.entity.*;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.WaitingRoomStatus;
import com.wornux.data.repository.ConsultationRepository;
import com.wornux.data.repository.EmployeeRepository;
import com.wornux.data.repository.PetRepository;
import com.wornux.data.repository.WaitingRoomRepository;
import com.wornux.dto.request.CreateConsultationRequestDto;
import com.wornux.dto.request.UpdateConsultationRequestDto;
import com.wornux.exception.EmployeeNotFoundException;
import com.wornux.exception.PetNotFoundException;
import com.wornux.mapper.ConsultationMapper;
import com.wornux.services.interfaces.ConsultationService;
import com.wornux.services.interfaces.EmployeeService;
import com.wornux.services.interfaces.MedicalHistoryService;
import com.wornux.services.interfaces.PetService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wornux.data.enums.ConsultationStatus;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed
public class ConsultationServiceImpl
    extends ListRepositoryService<Consultation, Long, ConsultationRepository>
    implements ConsultationService {
  private final ConsultationRepository consultationRepository;
  private final PetService petService;
  private final EmployeeService employeeService;
  private final MedicalHistoryService medicalHistoryService;
  private final PetRepository petRepository;
  private final ConsultationMapper consultationMapper;
  private final EmployeeRepository employeeRepository;
  private final WaitingRoomRepository waitingRoomRepository;

  public Consultation save(Consultation item) {
    log.debug("Saving Consultation: {}", item.getId());
    return consultationRepository.save(item);
  }

  @Override
  public Consultation create(CreateConsultationRequestDto createDTO) {
    log.debug("Request to create Consultation : {}", createDTO);

    Pet pet =
        petRepository
            .findById(createDTO.getPetId())
            .orElseThrow(() -> new PetNotFoundException(createDTO.getPetId()));

    Employee veterinarian =
        employeeRepository
            .findById(createDTO.getVeterinarianId())
            .orElseThrow(() -> new EmployeeNotFoundException(createDTO.getVeterinarianId()));

    MedicalHistory medicalHistory = medicalHistoryService.getOrCreateMedicalHistory(pet);

    Consultation consultation = consultationMapper.toEntity(createDTO);
    consultation.setPet(pet);
    consultation.setVeterinarian(veterinarian);
    consultation.setMedicalHistory(medicalHistory);

    Consultation savedConsultation = consultationRepository.save(consultation);

    updateMedicalHistoryFromConsultation(medicalHistory, savedConsultation);
    medicalHistoryService.updateMedicalHistory(medicalHistory);

    log.info(
        "Created Consultation with ID: {} for Pet: {}", savedConsultation.getId(), pet.getId());
    return savedConsultation;
  }

  /**
   * Updates the medical history with the information from the consultation.
   *
   * @param medicalHistory The medical history to update.
   * @param consultation The consultation containing the new information.
   */
  private void updateMedicalHistoryFromConsultation(
      MedicalHistory medicalHistory, Consultation consultation) {
    // Update medical history notes with consultation information
    String currentNotes = medicalHistory.getNotes() != null ? medicalHistory.getNotes() : "";
    String consultationInfo =
        String.format(
            "\n--- Consulta del %s ---\nDiagnóstico: %s\nTratamiento: %s\nPrescripción: %s\nNotas: %s\n",
            consultation
                .getConsultationDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            consultation.getDiagnosis() != null ? consultation.getDiagnosis() : "No especificado",
            consultation.getTreatment() != null ? consultation.getTreatment() : "No especificado",
            consultation.getPrescription() != null
                ? consultation.getPrescription()
                : "No especificado",
            consultation.getNotes() != null ? consultation.getNotes() : "No especificado");

    medicalHistory.setNotes(currentNotes + consultationInfo);
    medicalHistory.addConsultation(consultation);
  }

  @Override
  public Consultation update(Long id, UpdateConsultationRequestDto updateDTO) {
    log.debug("Request to update Consultation : {}", updateDTO);

    Consultation consultation = findById(id);

    Pet pet =
        petService
            .getPetById(updateDTO.getPetId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Pet not found with id: " + updateDTO.getPetId()));
    Employee veterinarian =
        employeeService
            .getEmployeeById(updateDTO.getVeterinarianId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Employee not found with id: " + updateDTO.getVeterinarianId()));

    consultation.setNotes(updateDTO.getNotes());
    consultation.setDiagnosis(updateDTO.getDiagnosis());
    consultation.setTreatment(updateDTO.getTreatment());
    consultation.setPrescription(updateDTO.getPrescription());
    try {
      consultation.setConsultationDate(LocalDateTime.parse(updateDTO.getConsultationDate()));
    } catch (Exception e) {
      log.error("Error parsing consultation date: {}", e.getMessage());
      throw new IllegalArgumentException(
          "Invalid date format. Use ISO format (yyyy-MM-ddTHH:mm:ss)");
    }
    consultation.setPet(pet);
    consultation.setVeterinarian(veterinarian);
    consultation.setUpdatedAt(LocalDateTime.now());

    return consultationRepository.save(consultation);
  }

  @Override
  public Consultation partialUpdate(Long id, UpdateConsultationRequestDto updateDTO) {
    log.debug("Request to partially update Consultation : {}", updateDTO);

    Consultation consultation = findById(id);

    if (updateDTO.getNotes() != null) {
      consultation.setNotes(updateDTO.getNotes());
    }

    if (updateDTO.getDiagnosis() != null) {
      consultation.setDiagnosis(updateDTO.getDiagnosis());
    }

    if (updateDTO.getTreatment() != null) {
      consultation.setTreatment(updateDTO.getTreatment());
    }

    if (updateDTO.getPrescription() != null) {
      consultation.setPrescription(updateDTO.getPrescription());
    }

    if (updateDTO.getConsultationDate() != null) {
      try {
        consultation.setConsultationDate(LocalDateTime.parse(updateDTO.getConsultationDate()));
      } catch (Exception e) {
        log.error("Error parsing consultation date: {}", e.getMessage());
        throw new IllegalArgumentException(
            "Invalid date format. Use ISO format (yyyy-MM-ddTHH:mm:ss)");
      }
    }

    if (updateDTO.getPetId() != null) {
      Pet pet =
          petService
              .getPetById(updateDTO.getPetId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Pet not found with id: " + updateDTO.getPetId()));
      consultation.setPet(pet);
    }

    if (updateDTO.getVeterinarianId() != null) {
      Employee veterinarian =
          employeeService
              .getEmployeeById(updateDTO.getVeterinarianId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Employee not found with id: " + updateDTO.getVeterinarianId()));
      consultation.setVeterinarian(veterinarian);
    }

    consultation.setUpdatedAt(LocalDateTime.now());

    return consultationRepository.save(consultation);
  }

  @Override
  public Consultation findById(Long id) {
    log.debug("Request to get Consultation : {}", id);
    return consultationRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Consultation not found with id: " + id));
  }

  @Override
  public Page<Consultation> findAll(Pageable pageable) {
    return consultationRepository.findByActiveTrue(pageable);
  }

  @Override
  public List<Consultation> findByPetId(Long petId) {
    return consultationRepository.findByPetIdAndActiveTrue(petId);
  }

  @Override
  public List<Consultation> findByVeterinarianId(Long veterinarianId) {
    return consultationRepository.findByVeterinarianIdAndActiveTrue(veterinarianId);
  }

  @Override
  public void delete(Long id) {
    log.debug("Request to deactivate Consultation : {}", id);
    Consultation consultation = findById(id);
    consultation.setActive(false);
    consultation.setUpdatedAt(LocalDateTime.now());
    consultationRepository.save(consultation);
  }

  public Page<Consultation> findByActiveTrue(Pageable pageable) {
    log.debug("Request to get all active Consultations");
    return consultationRepository.findByActiveTrue(pageable);
  }

  @Override
  public ConsultationRepository getRepository() {
    return consultationRepository;
  }

  @Override
  @Transactional
  public void assignFromWaitingRoom(Long waitingRoomId, Long veterinarianId) {
    WaitingRoom wr = waitingRoomRepository.findById(waitingRoomId)
            .orElseThrow(() -> new EntityNotFoundException("WaitingRoom no encontrado: " + waitingRoomId));

    if (wr.getStatus() != WaitingRoomStatus.ESPERANDO) {
      throw new IllegalStateException("Solo se puede asignar un veterinario cuando el estado es ESPERANDO.");
    }

    Employee vet = employeeRepository.findById(veterinarianId)
            .orElseThrow(() -> new EntityNotFoundException("Veterinario no encontrado: " + veterinarianId));

    if (vet.getEmployeeRole() != EmployeeRole.VETERINARIAN) {
      throw new IllegalStateException("El empleado seleccionado no es veterinario.");
    }
    if (!vet.isAvailable()) {
      throw new IllegalStateException("El veterinario seleccionado no está disponible.");
    }

    // Marcar como ocupado
    vet.setAvailable(false);
    employeeRepository.save(vet);

    // Solo guardar asignación en el waiting room
    wr.setAssignedVeterinarian(vet);
    waitingRoomRepository.save(wr);

    log.info("Veterinario {} asignado a WaitingRoom {}", vet.getId(), wr.getId());
  }


  private String buildInitialNotes(WaitingRoom wr, String extra) {
    StringBuilder sb = new StringBuilder();
    if (wr.getReasonForVisit() != null && !wr.getReasonForVisit().isBlank()) {
      sb.append("Motivo: ").append(wr.getReasonForVisit()).append('\n');
    }
    if (wr.getNotes() != null && !wr.getNotes().isBlank()) {
      sb.append("Notas recepción: ").append(wr.getNotes()).append('\n');
    }
    if (extra != null && !extra.isBlank()) {
      sb.append("Notas secretaria: ").append(extra);
    }
    return sb.toString();
  }

  @Override
  public Consultation start(Long waitingRoomId) {
    WaitingRoom wr = waitingRoomRepository.findById(waitingRoomId)
            .orElseThrow(() -> new EntityNotFoundException("WaitingRoom no encontrado: " + waitingRoomId));

    if (wr.getStatus() != WaitingRoomStatus.ESPERANDO) {
      throw new IllegalStateException("Solo se puede iniciar una consulta si el estado es ESPERANDO.");
    }

    wr.startConsultation();
    waitingRoomRepository.save(wr);

    Consultation c = new Consultation();
    c.setPet(wr.getPet());
    c.setVeterinarian(wr.getAssignedVeterinarian());
    c.setConsultationDate(LocalDateTime.now());
    c.setActive(true);
    c.setStatus(ConsultationStatus.EN_PROCESO);

    Consultation saved = consultationRepository.save(c);
    return saved;
  }

  @Override
  @Transactional
  public void finish(Long consultationId) {
    Consultation c = consultationRepository.findById(consultationId)
            .orElseThrow(() -> new EntityNotFoundException("Consultation not found: " + consultationId));

    // Marcar consulta finalizada
    c.setStatus(ConsultationStatus.COMPLETADO);
    c.setFinishedAt(LocalDateTime.now());
    //c.setActive(false);
    consultationRepository.save(c);

    // Cerrar WaitingRoom asociado
    WaitingRoom wr = c.getWaitingRoom();
    if (wr == null) {
      wr = waitingRoomRepository.findTopByPet_IdAndStatusInOrderByArrivalTimeDesc(
              c.getPet().getId(),
              List.of(WaitingRoomStatus.EN_CONSULTA, WaitingRoomStatus.ESPERANDO)
      ).orElse(null);
    }
    if (wr != null) {
      wr.completeConsultation();  
      waitingRoomRepository.save(wr);
    }

    // Liberar veterinario
    Employee vet = c.getVeterinarian();
    if (vet != null) {
      vet.setAvailable(true);
      employeeRepository.save(vet);
    }
  }



  @Override
  public List<Consultation> findForVeterinarian(Long veterinarianId) {
    return consultationRepository
            .findByVeterinarian_IdAndActiveTrueOrderByConsultationDateDesc(veterinarianId);
  }

}
