package com.wornux.services.implementations;

import com.wornux.data.entity.Consultation;
import com.wornux.data.entity.Invoice;
import com.wornux.data.entity.InvoiceOffering;
import com.wornux.data.repository.InvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

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
}
