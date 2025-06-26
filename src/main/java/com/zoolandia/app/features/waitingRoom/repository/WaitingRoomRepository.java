package com.zoolandia.app.features.waitingRoom.repository;

import com.zoolandia.app.features.client.domain.Client;
import com.zoolandia.app.features.waitingRoom.domain.WaitingRoom;
import com.zoolandia.app.features.waitingRoom.domain.WaitingRoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaitingRoomRepository extends JpaRepository<WaitingRoom, Long>, JpaSpecificationExecutor<WaitingRoom> {

    // Obtener lista actual de la sala de espera (esperando + en consulta)
    @Query("SELECT wr FROM WaitingRoom wr " + "JOIN FETCH wr.client c " + "JOIN FETCH wr.pet p "
            + "WHERE wr.status IN (:statuses) " + "ORDER BY wr.priority DESC, wr.arrivalTime ASC")
    List<WaitingRoom> findCurrentWaitingRoom(@Param("statuses") List<WaitingRoomStatus> statuses);

    // Obtener solo los que están esperando
    @Query("SELECT wr FROM WaitingRoom wr " + "JOIN FETCH wr.client c " + "JOIN FETCH wr.pet p "
            + "WHERE wr.status = 'WAITING' " + "ORDER BY wr.priority DESC, wr.arrivalTime ASC")
    List<WaitingRoom> findWaitingEntries();

    // Obtener los que están en consulta
    @Query("SELECT wr FROM WaitingRoom wr " + "JOIN FETCH wr.client c " + "JOIN FETCH wr.pet p "
            + "WHERE wr.status = 'IN_CONSULTATION' " + "ORDER BY wr.consultationStartedAt ASC")
    List<WaitingRoom> findInConsultationEntries();

    // Contar los que están esperando
    long countByStatus(WaitingRoomStatus status);

    // Buscar por cliente y mascota en estado esperando
    @Query("SELECT wr FROM WaitingRoom wr " + "WHERE wr.client.id = :clientId " + "AND wr.pet.id = :petId "
            + "AND wr.status = 'WAITING'")
    List<WaitingRoom> findWaitingByClientAndPet(@Param("clientId") Long clientId, @Param("petId") Long petId);

    // Obtener historial del día
    @Query("SELECT wr FROM WaitingRoom wr " + "JOIN FETCH wr.client c " + "JOIN FETCH wr.pet p "
            + "WHERE DATE(wr.arrivalTime) = CURRENT_DATE " + "ORDER BY wr.arrivalTime DESC")
    Page<WaitingRoom> findTodayHistory(Pageable pageable);
}