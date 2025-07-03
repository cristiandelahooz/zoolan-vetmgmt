package com.zoolandia.app.features.consultation.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.FormService;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.zoolandia.app.features.consultation.domain.Consultation;
import com.zoolandia.app.features.consultation.repository.ConsultationRepository;
import com.zoolandia.app.features.consultation.service.dto.CreateConsultationDTO;
import com.zoolandia.app.features.consultation.service.dto.UpdateConsultationDTO;
import com.zoolandia.app.features.employee.domain.Employee;
import com.zoolandia.app.features.employee.service.EmployeeService;
import com.zoolandia.app.features.pet.domain.Pet;
import com.zoolandia.app.features.pet.service.PetService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed
public class ConsultationServiceImpl extends ListRepositoryService<Consultation, Long, ConsultationRepository> implements ConsultationService, FormService<CreateConsultationDTO, Long> {
    private final ConsultationRepository consultationRepository;
    private final PetService petService;
    private final EmployeeService employeeService;


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

        Pet pet = petService.getPetById(createDTO.getPetId())
                .orElseThrow(() -> new EntityNotFoundException("Pet not found with id: " + createDTO.getPetId()));
        Employee veterinarian = employeeService.getEmployeeById(createDTO.getVeterinarianId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + createDTO.getVeterinarianId()));

        // Obtener o crear historial médico
        MedicalHistory medicalHistory = medicalHistoryService.getOrCreateMedicalHistory(pet);

        Consultation consultation = new Consultation();
        consultation.setNotes(createDTO.getNotes());
        consultation.setDiagnosis(createDTO.getDiagnosis());
        consultation.setTreatment(createDTO.getTreatment());
        consultation.setPrescription(createDTO.getPrescription());
        consultation.setConsultationDate(createDTO.getConsultationDate());
        consultation.setPet(pet);
        consultation.setVeterinarian(veterinarian);
        consultation.setMedicalHistory(medicalHistory);
        consultation.setCreatedAt(LocalDateTime.now());
        consultation.setUpdatedAt(LocalDateTime.now());
        consultation.setActive(true);

        Consultation savedConsultation = consultationRepository.save(consultation);

        // Agregar consulta al historial médico
        medicalHistory.addConsultation(savedConsultation);
        medicalHistoryService.updateMedicalHistory(medicalHistory);

        return savedConsultation;
    }

    @Override
    public Consultation update(Long id, UpdateConsultationDTO updateDTO) {
        log.debug("Request to update Consultation : {}", updateDTO);

        Consultation consultation = findById(id);

        Pet pet = petService.getPetById(updateDTO.getPetId())
                .orElseThrow(() -> new EntityNotFoundException("Pet not found with id: " + updateDTO.getPetId()));
        Employee veterinarian = employeeService.getEmployeeById(updateDTO.getVeterinarianId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + updateDTO.getVeterinarianId()));

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
            Employee veterinarian = employeeService.getEmployeeById(updateDTO.getVeterinarianId())
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + updateDTO.getVeterinarianId()));
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