package com.wornux.services.interfaces;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.data.enums.Priority;
import com.wornux.data.entity.WaitingRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link WaitingRoom} entities.
 */
@BrowserCallable
@Validated
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface WaitingRoomService {

    WaitingRoom addToWaitingRoom(Long clientId, Long petId, String reasonForVisit, Priority priority, String notes);

    List<WaitingRoom> getCurrentWaitingRoom();

    List<WaitingRoom> getWaitingEntries();

    List<WaitingRoom> getInConsultationEntries();

    WaitingRoom moveToConsultation(Long waitingRoomId);

    WaitingRoom completeConsultation(Long waitingRoomId);

    WaitingRoom cancelEntry(Long waitingRoomId, String reason);

    WaitingRoom updatePriority(Long waitingRoomId, Priority newPriority);

    WaitingRoom addNotes(Long waitingRoomId, String additionalNotes);

    Optional<WaitingRoom> getWaitingRoomById(Long id);

    Page<WaitingRoom> getTodayHistory(Pageable pageable);

    long getWaitingCount();

    long getInConsultationCount();

    long getTodayCount();

    double getAverageWaitTime();

    Page<WaitingRoom> searchWaitingRoom(String searchTerm, Pageable pageable);

    Page<WaitingRoom> getWaitingRoomByStatus(String status, Pageable pageable);

    /**
     * Save a waitingRoom.
     *
     * @param waitingRoom
     *         the entity to save.
     * @return the persisted entity.
     */
    WaitingRoom save(WaitingRoom waitingRoom);

    /**
     * Delete the waitingRoom by id.
     *
     * @param id
     *         the id of the entity.
     */
    void delete(Long id);

}