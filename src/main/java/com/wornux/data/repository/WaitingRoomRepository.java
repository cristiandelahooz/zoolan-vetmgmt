package com.wornux.data.repository;

import com.wornux.data.entity.WaitingRoom;
import com.wornux.data.enums.WaitingRoomStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitingRoomRepository extends JpaRepository<WaitingRoom, Long>, JpaSpecificationExecutor<WaitingRoom> {

    @Query("SELECT wr FROM WaitingRoom wr " + "JOIN FETCH wr.client c " + "JOIN FETCH wr.pet p " + "WHERE wr.status IN (:statuses) " + "ORDER BY wr.priority DESC, wr.arrivalTime ASC")
    List<WaitingRoom> findCurrentWaitingRoom(@Param("statuses") List<WaitingRoomStatus> statuses);

    @Query("SELECT wr FROM WaitingRoom wr " + "JOIN FETCH wr.client c " + "JOIN FETCH wr.pet p " + "WHERE wr.status = 'WAITING' " + "ORDER BY wr.priority DESC, wr.arrivalTime ASC")
    List<WaitingRoom> findWaitingEntries();

    @Query("SELECT wr FROM WaitingRoom wr " + "JOIN FETCH wr.client c " + "JOIN FETCH wr.pet p " + "WHERE wr.status = 'IN_CONSULTATION' " + "ORDER BY wr.consultationStartedAt ASC")
    List<WaitingRoom> findInConsultationEntries();

    long countByStatus(WaitingRoomStatus status);

    @Query("SELECT wr FROM WaitingRoom wr " + "WHERE wr.client.id = :clientId " + "AND wr.pet.id = :petId " + "AND wr.status = 'WAITING'")
    List<WaitingRoom> findWaitingByClientAndPet(@Param("clientId") Long clientId, @Param("petId") Long petId);

    @Query("SELECT wr FROM WaitingRoom wr " + "JOIN FETCH wr.client c " + "JOIN FETCH wr.pet p " + "WHERE wr.arrivalTime >= :startOfDay AND wr.arrivalTime < :endOfDay " + "ORDER BY wr.arrivalTime DESC")
    Page<WaitingRoom> findTodayHistory(@Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay, Pageable pageable);

    @Query("SELECT wr FROM WaitingRoom wr " + "WHERE wr.arrivalTime >= :startOfDay AND wr.arrivalTime < :endOfDay " + "ORDER BY wr.arrivalTime DESC")
    Page<WaitingRoom> findTodayHistorySimple(@Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay, Pageable pageable);
}
