package com.wornux.services.implementations;

import com.wornux.data.entity.Consultation;
import com.wornux.data.entity.Invoice;
import com.wornux.data.entity.InvoiceOffering;
import com.wornux.data.enums.InvoiceStatus;
import com.wornux.data.repository.InvoiceRepository;
import com.wornux.exception.InvalidInvoiceStatusChangeException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceService {

  @Getter private final InvoiceRepository repository;

  public Optional<Invoice> get(Long id) {
    if (id == null) return Optional.empty();

    return repository.findById(id);
  }

  public Page<Invoice> list(Pageable pageable) {
    return repository.findAll(pageable);
  }

  public Page<Invoice> list(Pageable pageable, Specification<Invoice> filter) {
    return repository.findAll(filter, pageable);
  }

  @Transactional
  public Invoice create(Invoice entity) {
    repository.save(entity);

    return entity;
  }

  public void delete(Long id) {
    Invoice entity =
        repository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Invoice not found."));

    entity.setActive(false);
    repository.save(entity);
  }

  public long getCount(Specification<Invoice> specification) {
    return repository.count(specification);
  }

  public String getNextInvoiceNumber() {
    long count = repository.count();
    return String.valueOf(count + 1);
  }

  public Invoice markInvoiceAsPaid(
      Invoice invoice, BigDecimal paymentAmount, LocalDate paymentDate) {
    invoice.markAsPaid(paymentAmount, paymentDate);
    return repository.save(invoice);
  }

  public Invoice markInvoiceAsOverdue(Invoice invoice) {
    invoice.markAsOverdue();
    return repository.save(invoice);
  }

  @Transactional()
  public Invoice findByConsultation(Consultation consultation) {
    return repository
        .findByConsultation(consultation)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Invoice not found for consultation ID: " + consultation.getId()));
  }

  @Transactional()
  public Invoice findByIdWithDetails(Long code) {

    return repository
        .findByCodeWithServicesAndProducts(code)
        .orElseThrow(() -> new EntityNotFoundException("Invoice not found with ID: " + code));
  }

  // Get invoice services count for display
  @Transactional()
  public long getServicesCount(Long invoiceId) {
    Invoice invoice = findByIdWithDetails(invoiceId);
    return invoice.getOfferings().size();
  }

  // Get invoice products count for display
  @Transactional()
  public long getProductsCount(Long invoiceId) {
    Invoice invoice = findByIdWithDetails(invoiceId);
    return invoice.getProducts().size();
  }

  // Calculate services total
  @Transactional()
  public BigDecimal calculateServicesTotal(Long invoiceId) {
    Invoice invoice = findByIdWithDetails(invoiceId);
    return invoice.getOfferings().stream()
        .map(InvoiceOffering::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Transactional
  public Optional<Invoice> findByGroomingId(Long groomingId) {
    return repository.findByGrooming_Id(groomingId);
  }

  @Transactional
  public Optional<Invoice> findByGroomingIdWithDetails(Long groomingId) {
    return repository.findByGroomingIdWithDetails(groomingId);
  }

  @Transactional
  public Optional<Invoice> findByConsultationIdWithDetails(Long consultationId) {
    return repository.findByConsultationIdWithDetails(consultationId);
  }

  @Transactional
  public Invoice changeInvoiceStatus(Long invoiceId, InvoiceStatus newStatus) {
    Invoice invoice =
        repository
            .findByCodeWithServicesAndProducts(invoiceId)
            .orElseThrow(
                () -> new EntityNotFoundException("Invoice not found with id: " + invoiceId));

    try {
      invoice.changeStatusTo(newStatus);
      return repository.save(invoice);
    } catch (InvalidInvoiceStatusChangeException ex) {
      throw ex;
    }
  }

  public BigDecimal getTotalOverdueAmount() {
    try {
      BigDecimal result = repository.findTotalOverdueAmount();
      return result != null ? result : BigDecimal.ZERO;
    } catch (Exception e) {
      return BigDecimal.ZERO;
    }
  }

  public BigDecimal getTotalAmountDueWithin30Days() {
    try {
      LocalDate targetDate = LocalDate.now().plusDays(30);
      BigDecimal result = repository.findTotalAmountDueByDate(targetDate);
      return result != null ? result : BigDecimal.ZERO;
    } catch (Exception e) {
      return BigDecimal.ZERO;
    }
  }

  public double getAveragePaymentTimeInDays() {
    try {
      List<Invoice> paidInvoices = repository.findAllPaidInvoices();
      
      if (paidInvoices == null || paidInvoices.isEmpty()) {
        return 0.0;
      }
      
      double totalDays = paidInvoices.stream()
          .filter(invoice -> invoice.getPaymentDate() != null && invoice.getIssuedDate() != null)
          .mapToLong(invoice -> ChronoUnit.DAYS.between(invoice.getIssuedDate(), invoice.getPaymentDate()))
          .filter(days -> days >= 0)
          .average()
          .orElse(0.0);
          
      return Math.max(0.0, totalDays);
    } catch (Exception e) {
      return 0.0;
    }
  }
}
