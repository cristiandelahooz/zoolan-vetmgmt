package com.wornux.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.FormService;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.wornux.domain.Consultation;
import com.wornux.mapper.ConsultationMapper;
import com.wornux.repository.ConsultationRepository;
import com.wornux.dto.CreateConsultationDTO;
import com.wornux.dto.UpdateConsultationDTO;
import com.wornux.domain.MedicalHistory;
import com.wornux.service.MedicalHistoryService;
import com.wornux.domain.Employee;
import com.wornux.repository.EmployeeRepository;
import com.wornux.service.EmployeeService;
import com.wornux.exception.EmployeeNotFoundException;
import com.wornux.domain.Pet;
import com.wornux.repository.PetRepository;
import com.wornux.service.PetService;

import com.wornux.exception.PetNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed
public class ConsultationServiceImpl extends ListRepositoryService<Consultation, Long, ConsultationRepository>
        implements ConsultationService, FormService<CreateConsultationDTO, Long> {
    private final ConsultationRepository consultationRepository;
    private final PetService petService;
    private final EmployeeService employeeService;
    private final MedicalHistoryService medicalHistoryService;
    private final PetRepository petRepository;
    private final ConsultationMapper consultationMapper;
    private final EmployeeRepository employeeRepository;

    @Override
    public CreateConsultationDTO save(CreateConsultationDTO item) {
        log.debug("Request to save Consultation : {}", item);

        // Create the consultation entity
        Consultation savedConsultation = create(item);

        // Convert back to DTO for return
        CreateConsultationDTO result = new CreateConsultationDTO();
        result.setPetId(savedConsultation.getPet().getId());
        result.setVeterinarianId(savedConsultation.getVeterinarian().getId());
        result.setNotes(savedConsultation.getNotes());
        result.setDiagnosis(savedConsultation.getDiagnosis());
        result.setTreatment(savedConsultation.getTreatment());
        result.setPrescription(savedConsultation.getPrescription());
        result.setConsultationDate(savedConsultation.getConsultationDate());

        return result;
    }

    @Override
    public Consultation create(CreateConsultationDTO createDTO) {
        log.debug("Request to create Consultation : {}", createDTO);

        Pet pet = petRepository.findById(createDTO.getPetId())
                .orElseThrow(() -> new PetNotFoundException(createDTO.getPetId()));

        Employee veterinarian = employeeRepository.findById(createDTO.getVeterinarianId())
                .orElseThrow(() -> new EmployeeNotFoundException(createDTO.getVeterinarianId()));

        MedicalHistory medicalHistory = medicalHistoryService.getOrCreateMedicalHistory(pet);

        Consultation consultation = consultationMapper.toEntity(createDTO);
        consultation.setPet(pet);
        consultation.setVeterinarian(veterinarian);
        consultation.setMedicalHistory(medicalHistory);

        Consultation savedConsultation = consultationRepository.save(consultation);

        updateMedicalHistoryFromConsultation(medicalHistory, savedConsultation);
        medicalHistoryService.updateMedicalHistory(medicalHistory);

        log.info("Created Consultation with ID: {} for Pet: {}", savedConsultation.getId(), pet.getId());
        return savedConsultation;
    }

    /**
     * Updates the medical history with the information from the consultation.
     *
     * @param medicalHistory
     *            The medical history to update.
     * @param consultation
     *            The consultation containing the new information.
     */
    private void updateMedicalHistoryFromConsultation(MedicalHistory medicalHistory, Consultation consultation) {
        // Update medical history notes with consultation information
        String currentNotes = medicalHistory.getNotes() != null ? medicalHistory.getNotes() : "";
        String consultationInfo = String.format(
                "\n--- Consulta del %s ---\nDiagnóstico: %s\nTratamiento: %s\nPrescripción: %s\nNotas: %s\n",
                consultation.getConsultationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                consultation.getDiagnosis() != null ? consultation.getDiagnosis() : "No especificado",
                consultation.getTreatment() != null ? consultation.getTreatment() : "No especificado",
                consultation.getPrescription() != null ? consultation.getPrescription() : "No especificado",
                consultation.getNotes() != null ? consultation.getNotes() : "No especificado");

        medicalHistory.setNotes(currentNotes + consultationInfo);
        medicalHistory.addConsultation(consultation);
    }

    @Override
    public Consultation update(Long id, UpdateConsultationDTO updateDTO) {
        log.debug("Request to update Consultation : {}", updateDTO);

        Consultation consultation = findById(id);

        Pet pet = petService.getPetById(updateDTO.getPetId())
                .orElseThrow(() -> new EntityNotFoundException("Pet not found with id: " + updateDTO.getPetId()));
        Employee veterinarian = employeeService.getEmployeeById(updateDTO.getVeterinarianId()).orElseThrow(
                () -> new EntityNotFoundException("Employee not found with id: " + updateDTO.getVeterinarianId()));

        consultation.setNotes(updateDTO.getNotes());
        consultation.setDiagnosis(updateDTO.getDiagnosis());
        consultation.setTreatment(updateDTO.getTreatment());
        consultation.setPrescription(updateDTO.getPrescription());
        try {
            consultation.setConsultationDate(LocalDateTime.parse(updateDTO.getConsultationDate()));
        } catch (Exception e) {
            log.error("Error parsing consultation date: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid date format. Use ISO format (yyyy-MM-ddTHH:mm:ss)");
        }
        consultation.setPet(pet);
        consultation.setVeterinarian(veterinarian);
        consultation.setUpdatedAt(LocalDateTime.now());

        return consultationRepository.save(consultation);
    }

    @Override
    public Consultation partialUpdate(Long id, UpdateConsultationDTO updateDTO) {
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
                throw new IllegalArgumentException("Invalid date format. Use ISO format (yyyy-MM-ddTHH:mm:ss)");
            }
        }

        if (updateDTO.getPetId() != null) {
            Pet pet = petService.getPetById(updateDTO.getPetId())
                    .orElseThrow(() -> new EntityNotFoundException("Pet not found with id: " + updateDTO.getPetId()));
            consultation.setPet(pet);
        }

        if (updateDTO.getVeterinarianId() != null) {
            Employee veterinarian = employeeService.getEmployeeById(updateDTO.getVeterinarianId()).orElseThrow(
                    () -> new EntityNotFoundException("Employee not found with id: " + updateDTO.getVeterinarianId()));
            consultation.setVeterinarian(veterinarian);
        }

        consultation.setUpdatedAt(LocalDateTime.now());

        return consultationRepository.save(consultation);
    }

    @Override
    public Consultation findById(Long id) {
        log.debug("Request to get Consultation : {}", id);
        return consultationRepository.findById(id)
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

}