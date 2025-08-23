package com.wornux.data.repository;

import com.wornux.data.entity.Consultation;
import com.wornux.data.entity.Invoice;

import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository
    extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

  @EntityGraph(attributePaths = {"client", "products.product", "services.service"})
  @Query("SELECT i FROM Invoice i WHERE i.code = :code")
  Optional<Invoice> findByCodeWithServicesAndProducts(@Param("code") Long code);

  @EntityGraph(attributePaths = {"client", "products.product", "services.service"})
  @Query("SELECT i FROM Invoice i WHERE i.consultation = :consultation AND i.active = true")
  Optional<Invoice> findByConsultation(@Param("consultation") Consultation consultation);

  @Query("SELECT DATE(i.createdDate) as period, SUM(i.total) as revenue, COUNT(i) as invoiceCount " +
      "FROM Invoice i WHERE i.createdDate >= :startDate AND i.createdDate <= :endDate " +
      "GROUP BY DATE(i.createdDate) ORDER BY DATE(i.createdDate)")
  List<Object[]> findMonthlyRevenue(@Param("startDate") Instant startDate,
                                    @Param("endDate") Instant endDate);

  @Query("SELECT s.name, SUM(is.amount) as revenue " +
      "FROM ServiceInvoice is JOIN is.service s JOIN is.invoice i " +
      "WHERE i.createdDate >= :startDate " +
      "GROUP BY s.id, s.name ORDER BY revenue DESC")
  List<Object[]> findTopServicesByRevenue(@Param("startDate") Instant startDate,
                                          Pageable pageable);
}
