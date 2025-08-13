package com.wornux.services.implementations;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.FormService;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.wornux.data.entity.Client;
import com.wornux.data.repository.ClientRepository;
import com.wornux.data.entity.Pet;
import com.wornux.data.repository.PetRepository;
import com.wornux.data.entity.WaitingRoom;
import com.wornux.data.enums.WaitingRoomStatus;
import com.wornux.data.repository.WaitingRoomRepository;
import com.wornux.dto.request.WaitingRoomCreateRequestDto;
import com.wornux.exception.WaitingRoomNotFoundException;
import com.wornux.mapper.WaitingRoomMapper;
import com.wornux.services.interfaces.ConsultationService;
import com.wornux.services.interfaces.WaitingRoomService;
import org.jspecify.annotations.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import com.wornux.data.enums.Priority;

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
@Transactional
// TODO: Remove @AnonymousAllowed and restrict access before deploying to production. This is only for development/testing purposes.
public class WaitingRoomServiceImpl extends ListRepositoryService<WaitingRoom, Long, WaitingRoomRepository>
        implements WaitingRoomService {

    private final WaitingRoomRepository waitingRoomRepository;
    private final ClientRepository clientRepository;
    private final PetRepository petRepository;
    private final WaitingRoomMapper waitingRoomMapper;

    @Override
    public WaitingRoom save(WaitingRoomCreateRequestDto dto) {
        log.debug("Request to save WaitingRoom: {}", dto);

        WaitingRoom waitingRoom = waitingRoomMapper.toEntity(dto, clientRepository, petRepository);

        if (waitingRoom.getPet() != null && waitingRoom.getClient() != null) {
            boolean petBelongsToClient = waitingRoom.getPet().getOwners().stream()
                    .anyMatch(owner -> owner.getId().equals(waitingRoom.getClient().getId()));
            if (!petBelongsToClient) {
                throw new IllegalArgumentException("La mascota no pertenece al cliente especificado");
            }
        }

        List<WaitingRoom> existing = waitingRoomRepository
                .findWaitingByClientAndPet(dto.getClientId(), dto.getPetId());
        if (!existing.isEmpty()) {
            throw new IllegalStateException("El cliente y mascota ya están en la sala de espera");
        }

        WaitingRoom saved = waitingRoomRepository.save(waitingRoom);
        log.info("WaitingRoom saved with ID: {}", saved.getId());
        return saved;
    }


    @Override
    @Transactional(readOnly = true)
    public List<WaitingRoom> getCurrentWaitingRoom() {
        log.debug("Request to get current waiting room");

        List<WaitingRoomStatus> activeStatuses = Arrays.asList(WaitingRoomStatus.ESPERANDO,
                WaitingRoomStatus.EN_CONSULTA);

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
    //@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'VETERINARIAN')")
    public WaitingRoom moveToConsultation(Long waitingRoomId) {
        log.debug("Request to move to consultation: {}", waitingRoomId);

        WaitingRoom waitingRoom = waitingRoomRepository.findById(waitingRoomId)
                .orElseThrow(() -> new WaitingRoomNotFoundException(waitingRoomId));

        if (waitingRoom.getStatus() != WaitingRoomStatus.ESPERANDO) {
            throw new IllegalStateException("Solo se pueden mover a consulta las entradas en estado 'WAITING'");
        }

        waitingRoom.startConsultation();
        WaitingRoom updated = waitingRoomRepository.save(waitingRoom);

        log.info("Movido a consulta: {} - {} con {}", waitingRoomId, waitingRoom.getClient().getFirstName(),
                waitingRoom.getPet().getName());
        return updated;
    }

    @Override
    //@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'VETERINARIAN')")
    public WaitingRoom completeConsultation(Long waitingRoomId) {
        log.debug("Request to complete consultation: {}", waitingRoomId);

        WaitingRoom waitingRoom = waitingRoomRepository.findById(waitingRoomId)
                .orElseThrow(() -> new WaitingRoomNotFoundException(waitingRoomId));

        if (waitingRoom.getStatus() != WaitingRoomStatus.EN_CONSULTA) {
            throw new IllegalStateException("Solo se pueden completar las consultas en estado 'IN_CONSULTATION'");
        }

        waitingRoom.completeConsultation();
        WaitingRoom updated = waitingRoomRepository.save(waitingRoom);

        log.info("Consulta completada: {} - {} con {}", waitingRoomId, waitingRoom.getClient().getFirstName(),
                waitingRoom.getPet().getName());
        return updated;
    }

    @Override
    //@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public WaitingRoom cancelEntry(Long waitingRoomId, String reason) {
        log.debug("Request to cancel entry: {}", waitingRoomId);

        WaitingRoom waitingRoom = waitingRoomRepository.findById(waitingRoomId)
                .orElseThrow(() -> new WaitingRoomNotFoundException(waitingRoomId));

        if (waitingRoom.getStatus() == WaitingRoomStatus.COMPLETADO
                || waitingRoom.getStatus() == WaitingRoomStatus.CANCELADO) {
            throw new IllegalStateException("No se puede cancelar una entrada ya completada o cancelada");
        }

        waitingRoom.setStatus(WaitingRoomStatus.CANCELADO);
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
        return waitingRoomRepository.countByStatus(WaitingRoomStatus.ESPERANDO);
    }

    @Override
    @Transactional(readOnly = true)
    public long getInConsultationCount() {
        return waitingRoomRepository.countByStatus(WaitingRoomStatus.EN_CONSULTA);
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
                    .filter(wr -> wr.getStatus() == WaitingRoomStatus.COMPLETADO
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
    public void delete(Long id) {
        log.debug("Request to soft delete WaitingRoom: {}", id);

        WaitingRoom waitingRoom = waitingRoomRepository.findById(id)
                .orElseThrow(() -> new WaitingRoomNotFoundException(id));

        if (waitingRoom.getStatus() == WaitingRoomStatus.EN_CONSULTA) {
            throw new IllegalStateException("No se puede eliminar una entrada que está en consulta activa");
        }

        waitingRoom.setStatus(WaitingRoomStatus.CANCELADO);
        waitingRoomRepository.save(waitingRoom);

        log.info("WaitingRoom soft deleted with ID: {}", id);
    }



    @Override
    public void update(WaitingRoom waitingRoom) {
        waitingRoomRepository.save(waitingRoom);
    }

    @Override
    public WaitingRoomRepository getRepository() {
        return waitingRoomRepository;
    }
}