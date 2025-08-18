package com.wornux.data.entity;

import com.wornux.data.enums.InvoiceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@ToString
@Audited(withModifiedFlag = true)
@Builder
@Table(
    name = "invoices",
    indexes = {
        @Index(columnList = "issuedDate"),
        @Index(columnList = "paymentDate"),
        @Index(columnList = "status")
    })
@NoArgsConstructor
@AllArgsConstructor
public class Invoice extends Auditable implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long code;

  @NotNull
  @ToString.Exclude
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "client", referencedColumnName = "client_id", nullable = false)
  private Client client;

  @ToString.Exclude
  @OneToMany(
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER,
      orphanRemoval = true,
      mappedBy = "invoice")
  private Set<InvoiceProduct> products = new HashSet<>();

  @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @Builder.Default
  private List<ServiceInvoice> services = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "consultation", referencedColumnName = "id")
  private Consultation consultation;

  @Column(name = "consultation_notes", length = 1000)
  private String consultationNotes;

  @ToString.Exclude
  @OneToMany(
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true,
      mappedBy = "invoice")
  private Set<PaymentDetail> paymentDetails = new HashSet<>();

  @NotNull
  private LocalDate issuedDate;

  @NotNull
  private LocalDate paymentDate;

  @Size(max = 100)
  private String salesOrder;

  @NotNull
  @Enumerated(EnumType.STRING)
  private InvoiceStatus status = InvoiceStatus.DRAFT;

  @NotNull
  private BigDecimal subtotal;

  private BigDecimal discountPercentage;

  private BigDecimal discount;

  @NotNull
  private BigDecimal tax;

  @NotNull
  private BigDecimal total;

  @NotNull
  private BigDecimal paidToDate = BigDecimal.ZERO;

  @Size(max = 500)
  private String notes;

  public void markAsPaid(BigDecimal paymentAmount, LocalDate paymentDate) {
    if (this.status != InvoiceStatus.PENDING) {
      throw new IllegalStateException(
          "Solo las facturas pendientes pueden ser marcadas como pagadas.");
    }
    if (paymentDate.isBefore(this.issuedDate)) {
      throw new IllegalArgumentException("El pago no puede ser anterior a la fecha de emisión.");
    }
    this.paidToDate = this.paidToDate.add(paymentAmount);
    if (this.paidToDate.compareTo(this.total) >= 0) {
      this.status = InvoiceStatus.PAID;
      this.paymentDate = paymentDate;
    }
  }

  public void markAsOverdue() {
    if (this.status != InvoiceStatus.PENDING) {
      throw new IllegalStateException(
          "Solo las facturas pendientes pueden ser marcadas como atrasadas.");
    }
    if (!LocalDate.now().isAfter(this.paymentDate)) {
      throw new IllegalArgumentException("La factura no está atrasada");
    }
    this.status = InvoiceStatus.OVERDUE;
  }

  public BigDecimal getTotalPaid() {
    return paymentDetails.stream()
        .map(PaymentDetail::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public BigDecimal getOutstandingBalance() {
    return this.total.subtract(getTotalPaid());
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    Class<?> oEffectiveClass =
        o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) {
      return false;
    }
    Invoice that = (Invoice) o;
    return getCode() != null && Objects.equals(getCode(), that.getCode());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }

  // Helper methods for services management
  public void addService(ServiceInvoice serviceInvoice) {
    if (services == null) {
      services = new ArrayList<>();
    }
    services.add(serviceInvoice);
    serviceInvoice.setInvoice(this);
  }

  public void removeService(ServiceInvoice serviceInvoice) {
    if (services != null) {
      services.remove(serviceInvoice);
      serviceInvoice.setInvoice(null);
    }
  }

  // Enhanced calculateTotals method to include services
  public void calculateTotals() {
    // Calculate products subtotal
    BigDecimal productsSubtotal = products.stream()
        .map(InvoiceProduct::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Calculate services subtotal
    BigDecimal servicesSubtotal = services.stream()
        .map(ServiceInvoice::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Total subtotal
    this.subtotal = productsSubtotal.add(servicesSubtotal);

    // Calculate discount
    if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
      this.discount = subtotal.multiply(discountPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    } else {
      this.discount = BigDecimal.ZERO;
    }

    // Calculate tax (assuming 18% ITBIS)
    BigDecimal taxableAmount = subtotal.subtract(discount != null ? discount : BigDecimal.ZERO);
    this.tax = taxableAmount.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);

    // Calculate total
    this.total = taxableAmount.add(tax);
  }

  public void addProduct(InvoiceProduct product) {
    if (products == null) {
      products = new HashSet<>();
    }
    products.add(product);
    product.setInvoice(this);
  }

}
