package com.wornux.features.waitingRoom.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.FormService;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.wornux.features.client.domain.Client;
import com.wornux.features.client.repository.ClientRepository;
import com.wornux.features.pet.domain.Pet;
import com.wornux.features.pet.repository.PetRepository;
import com.wornux.features.waitingRoom.domain.WaitingRoom;
import com.wornux.features.waitingRoom.domain.WaitingRoomStatus;
import com.wornux.features.waitingRoom.repository.WaitingRoomRepository;
import com.wornux.features.waitingRoom.service.dto.WaitingRoomCreateDTO;
import com.wornux.features.waitingRoom.service.exception.WaitingRoomNotFoundException;
import org.jspecify.annotations.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import com.wornux.features.waitingRoom.domain.Priority;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed
// TODO: Remove @AnonymousAllowed and restrict access before deploying to production. This is only for development/testing purposes.
public class WaitingRoomServiceImpl extends ListRepositoryService<WaitingRoom, Long, WaitingRoomRepository>
        implements WaitingRoomService, FormService<WaitingRoomCreateDTO, Long> {

    private final WaitingRoomRepository waitingRoomRepository;
    private final ClientRepository clientRepository;
    private final PetRepository petRepository;

    @Override
    @Transactional
    public @Nullable WaitingRoomCreateDTO save(WaitingRoomCreateDTO dto) {
        try {
            log.debug("Request to save WaitingRoom via FormService: {}", dto);

            WaitingRoom saved = addToWaitingRoom(dto.clientId(), dto.petId(), dto.reasonForVisit(), dto.priority(),
                    dto.notes());

            WaitingRoomCreateDTO result = new WaitingRoomCreateDTO(saved.getClient().getId(), saved.getPet().getId(),
                    saved.getReasonForVisit(), saved.getPriority(), saved.getNotes());

            log.info("WaitingRoom saved successfully via FormService with ID: {}", saved.getId());
            return result;

        } catch (Exception e) {
            log.error("Error saving WaitingRoom via FormService: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    //@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public WaitingRoom addToWaitingRoom(Long clientId, Long petId, String reasonForVisit, Priority priority,
            String notes) {
        log.debug("Request to add to waiting room - Client: {}, Pet: {}", clientId, petId);

        List<WaitingRoom> existing = waitingRoomRepository.findWaitingByClientAndPet(clientId, petId);
        if (!existing.isEmpty()) {
            log.warn("Cliente y mascota ya están en la sala de espera");
            throw new IllegalStateException("El cliente y mascota ya están en la sala de espera");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + clientId));

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con ID: " + petId));

        boolean petBelongsToClient = pet.getOwners().stream().anyMatch(owner -> owner.getId().equals(clientId));

        if (!petBelongsToClient) {
            throw new IllegalArgumentException("La mascota no pertenece al cliente especificado");
        }

        WaitingRoom waitingRoom = new WaitingRoom();
        waitingRoom.setClient(client);
        waitingRoom.setPet(pet);
        waitingRoom.setReasonForVisit(reasonForVisit);
        waitingRoom.setPriority(priority != null ? priority : Priority.NORMAL);
        waitingRoom.setNotes(notes);
        waitingRoom.setStatus(WaitingRoomStatus.WAITING);

        WaitingRoom saved = waitingRoomRepository.save(waitingRoom);
        log.info("Agregado a sala de espera con ID: {} para {} - {}", saved.getId(), client.getFirstName(),
                pet.getName());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaitingRoom> getCurrentWaitingRoom() {
        log.debug("Request to get current waiting room");

        List<WaitingRoomStatus> activeStatuses = Arrays.asList(WaitingRoomStatus.WAITING,
                WaitingRoomStatus.IN_CONSULTATION);

        List<WaitingRoom> waitingList = waitingRoomRepository.findCurrentWaitingRoom(activeStatuses);

        waitingList.forEach(wr -> {
            if (wr.getClient() != null) {
                wr.getClient().getFirstName();
            }
            if (wr.getPet() != null) {
                wr.getPet().getName();
            }
        });

        log.debug("Found {} entries in current waiting room", waitingList.size());
        return waitingList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaitingRoom> getWaitingEntries() {
        log.debug("Request to get waiting entries");
        return waitingRoomRepository.findWaitingEntries();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaitingRoom> getInConsultationEntries() {
        log.debug("Request to get in-consultation entries");
        return waitingRoomRepository.findInConsultationEntries();
    }

    @Override
    @Transactional
    //@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'VETERINARIAN')")
    public WaitingRoom moveToConsultation(Long waitingRoomId) {
        log.debug("Request to move to consultation: {}", waitingRoomId);

        WaitingRoom waitingRoom = waitingRoomRepository.findById(waitingRoomId)
                .orElseThrow(() -> new WaitingRoomNotFoundException(waitingRoomId));

        if (waitingRoom.getStatus() != WaitingRoomStatus.WAITING) {
            throw new IllegalStateException("Solo se pueden mover a consulta las entradas en estado 'WAITING'");
        }

        waitingRoom.startConsultation();
        WaitingRoom updated = waitingRoomRepository.save(waitingRoom);

        log.info("Movido a consulta: {} - {} con {}", waitingRoomId, waitingRoom.getClient().getFirstName(),
                waitingRoom.getPet().getName());
        return updated;
    }

    @Override
    @Transactional
    //@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'VETERINARIAN')")
    public WaitingRoom completeConsultation(Long waitingRoomId) {
        log.debug("Request to complete consultation: {}", waitingRoomId);

        WaitingRoom waitingRoom = waitingRoomRepository.findById(waitingRoomId)
                .orElseThrow(() -> new WaitingRoomNotFoundException(waitingRoomId));

        if (waitingRoom.getStatus() != WaitingRoomStatus.IN_CONSULTATION) {
            throw new IllegalStateException("Solo se pueden completar las consultas en estado 'IN_CONSULTATION'");
        }

        waitingRoom.completeConsultation();
        WaitingRoom updated = waitingRoomRepository.save(waitingRoom);

        log.info("Consulta completada: {} - {} con {}", waitingRoomId, waitingRoom.getClient().getFirstName(),
                waitingRoom.getPet().getName());
        return updated;
    }

    @Override
    @Transactional
    //@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public WaitingRoom cancelEntry(Long waitingRoomId, String reason) {
        log.debug("Request to cancel entry: {}", waitingRoomId);

        WaitingRoom waitingRoom = waitingRoomRepository.findById(waitingRoomId)
                .orElseThrow(() -> new WaitingRoomNotFoundException(waitingRoomId));

        if (waitingRoom.getStatus() == WaitingRoomStatus.COMPLETED
            || waitingRoom.getStatus() == WaitingRoomStatus.CANCELLED) {
            throw new IllegalStateException("No se puede cancelar una entrada ya completada o cancelada");
        }

        waitingRoom.setStatus(WaitingRoomStatus.CANCELLED);
        waitingRoom.setCompletedAt(LocalDateTime.now());

        String currentNotes = waitingRoom.getNotes();
        String newNotes = (currentNotes != null ? currentNotes + "\n" : "") + "Cancelado el " + LocalDateTime.now()
                          + ": " + reason;
        waitingRoom.setNotes(newNotes);

        WaitingRoom updated = waitingRoomRepository.save(waitingRoom);

        log.info("Entrada cancelada: {} - Razón: {}", waitingRoomId, reason);
        return updated;
    }

    @Override
    @Transactional
    public WaitingRoom updatePriority(Long waitingRoomId, Priority newPriority) {
        log.debug("Request to update priority: {} to {}", waitingRoomId, newPriority);

        WaitingRoom waitingRoom = waitingRoomRepository.findById(waitingRoomId)
                .orElseThrow(() -> new WaitingRoomNotFoundException(waitingRoomId));

        if (newPriority == null) {
            throw new IllegalArgumentException("La prioridad no puede ser nula");
        }

        waitingRoom.setPriority(newPriority);
        WaitingRoom updated = waitingRoomRepository.save(waitingRoom);

        log.info("Prioridad actualizada para ID: {} a {}", waitingRoomId, newPriority);
        return updated;
    }

    @Override
    @Transactional
    //@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public WaitingRoom addNotes(Long waitingRoomId, String additionalNotes) {
        log.debug("Request to add notes to: {}", waitingRoomId);

        WaitingRoom waitingRoom = waitingRoomRepository.findById(waitingRoomId)
                .orElseThrow(() -> new WaitingRoomNotFoundException(waitingRoomId));

        String currentNotes = waitingRoom.getNotes();
        String newNotes = (currentNotes != null ? currentNotes + "\n" : "") + LocalDateTime.now() + " - "
                          + additionalNotes;

        waitingRoom.setNotes(newNotes);
        WaitingRoom updated = waitingRoomRepository.save(waitingRoom);

        log.info("Notas agregadas para ID: {}", waitingRoomId);
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WaitingRoom> getWaitingRoomById(Long id) {
        log.debug("Request to get WaitingRoom: {}", id);
        return waitingRoomRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long getWaitingCount() {
        return waitingRoomRepository.countByStatus(WaitingRoomStatus.WAITING);
    }

    @Override
    @Transactional(readOnly = true)
    public long getInConsultationCount() {
        return waitingRoomRepository.countByStatus(WaitingRoomStatus.IN_CONSULTATION);
    }

    @Override
    @Transactional(readOnly = true)
    public double getAverageWaitTime() {
        return calculateAverageWaitTime();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WaitingRoom> searchWaitingRoom(String searchTerm, Pageable pageable) {
        log.debug("Request to search waiting room with term: {}", searchTerm);

        Specification<WaitingRoom> spec = (root, query, cb) -> {
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return cb.or(cb.like(cb.lower(root.get("client").get("firstName")), pattern),
                    cb.like(cb.lower(root.get("client").get("lastName")), pattern),
                    cb.like(cb.lower(root.get("pet").get("name")), pattern),
                    cb.like(cb.lower(root.get("reasonForVisit")), pattern));
        };

        return waitingRoomRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WaitingRoom> getWaitingRoomByStatus(String status, Pageable pageable) {
        log.debug("Request to get waiting room by status: {}", status);

        WaitingRoomStatus waitingRoomStatus = WaitingRoomStatus.valueOf(status.toUpperCase());

        Specification<WaitingRoom> spec = (root, query, cb) -> cb.equal(root.get("status"), waitingRoomStatus);

        return waitingRoomRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WaitingRoom> getTodayHistory(Pageable pageable) {
        log.debug("Request to get today's waiting room history");

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return waitingRoomRepository.findTodayHistory(startOfDay, endOfDay, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTodayCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        Page<WaitingRoom> todayEntries = waitingRoomRepository.findTodayHistory(startOfDay, endOfDay,
                Pageable.unpaged());
        return todayEntries.getTotalElements();
    }

    private double calculateAverageWaitTime() {
        try {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            List<WaitingRoom> completedToday = waitingRoomRepository
                    .findTodayHistory(startOfDay, endOfDay, Pageable.unpaged()).getContent().stream()
                    .filter(wr -> wr.getStatus() == WaitingRoomStatus.COMPLETED
                                  && wr.getConsultationStartedAt() != null)
                    .toList();

            if (completedToday.isEmpty()) {
                return 0.0;
            }

            double totalMinutes = completedToday.stream()
                    .mapToDouble(wr -> Duration.between(wr.getArrivalTime(), wr.getConsultationStartedAt()).toMinutes())
                    .average().orElse(0.0);

            return Math.round(totalMinutes * 100.0) / 100.0;

        } catch (Exception e) {
            log.warn("Error calculando tiempo promedio de espera: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    @Transactional
    public WaitingRoom save(WaitingRoom waitingRoom) {
        log.debug("Request to save WaitingRoom: {}", waitingRoom);

        if (waitingRoom.getId() == null) {
            if (waitingRoom.getClient() != null && waitingRoom.getPet() != null) {
                List<WaitingRoom> existing = waitingRoomRepository
                        .findWaitingByClientAndPet(waitingRoom.getClient().getId(), waitingRoom.getPet().getId());
                if (!existing.isEmpty()) {
                    throw new IllegalStateException("El cliente y mascota ya están en la sala de espera");
                }
            }

            if (waitingRoom.getStatus() == null) {
                waitingRoom.setStatus(WaitingRoomStatus.WAITING);
            }
            if (waitingRoom.getPriority() == null) {
                waitingRoom.setPriority(Priority.NORMAL);
            }
            if (waitingRoom.getArrivalTime() == null) {
                waitingRoom.setArrivalTime(LocalDateTime.now());
            }
        }

        WaitingRoom saved = waitingRoomRepository.save(waitingRoom);
        log.info("WaitingRoom saved with ID: {}", saved.getId());

        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete WaitingRoom: {}", id);

        WaitingRoom waitingRoom = waitingRoomRepository.findById(id)
                .orElseThrow(() -> new WaitingRoomNotFoundException(id));

        if (waitingRoom.getStatus() == WaitingRoomStatus.IN_CONSULTATION) {
            throw new IllegalStateException("No se puede eliminar una entrada que está en consulta activa");
        }

        waitingRoomRepository.deleteById(id);
        log.info("WaitingRoom deleted with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WaitingRoom> get(Long id) {
        log.debug("Request to get WaitingRoom: {}", id);
        return waitingRoomRepository.findById(id);
    }
}