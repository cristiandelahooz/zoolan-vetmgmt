package com.wornux.data.entity;

import com.wornux.data.enums.InvoiceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@Entity
@Audited(withModifiedFlag = true)
@Builder
@Table(name = "invoices", indexes = { @Index(columnList = "issuedDate"), @Index(columnList = "paymentDate"),
        @Index(columnList = "status") })
@NoArgsConstructor
@AllArgsConstructor
public class Invoice extends Auditable implements Serializable {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.18").setScale(2, RoundingMode.HALF_UP);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long code;
    @NotNull
    @ToString.Exclude
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "client", referencedColumnName = "client_id", nullable = false)
    private Client client;
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "invoice")
    @Builder.Default
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
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "invoice")
    @Builder.Default
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
            throw new IllegalStateException("Solo las facturas pendientes pueden ser marcadas como pagadas.");
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
            throw new IllegalStateException("Solo las facturas pendientes pueden ser marcadas como atrasadas.");
        }
        if (!LocalDate.now().isAfter(this.paymentDate)) {
            throw new IllegalArgumentException("La factura no está atrasada");
        }
        this.status = InvoiceStatus.OVERDUE;
    }

    public BigDecimal getTotalPaid() {
        return paymentDetails.stream().map(PaymentDetail::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
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
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer()
                .getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) {
            return false;
        }
        Invoice that = (Invoice) o;
        return getCode() != null && Objects.equals(getCode(), that.getCode());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass().hashCode() : getClass().hashCode();
    }

    public void addService(ServiceInvoice serviceInvoice) {
        services.add(serviceInvoice);
        serviceInvoice.setInvoice(this);
        serviceInvoice.calculateAmount();
    }

    public void removeService(ServiceInvoice serviceInvoice) {
        services.remove(serviceInvoice);
        serviceInvoice.setInvoice(null);
    }

    public void calculateTotals() {
        BigDecimal servicesTotal = services.stream().map(ServiceInvoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal productsTotal = products.stream().map(InvoiceProduct::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.subtotal = servicesTotal.add(productsTotal);
        this.tax = this.subtotal.multiply(TAX_RATE);
        this.total = this.subtotal.add(this.tax);
    }

    public void addProduct(InvoiceProduct productToAdd) {
        Optional<InvoiceProduct> existingProductOpt = products.stream()
                .filter(p -> p.getProduct().equals(productToAdd.getProduct())).findFirst();

        if (existingProductOpt.isPresent()) {
            InvoiceProduct existingProduct = existingProductOpt.get();
            existingProduct.setQuantity(existingProduct.getQuantity() + productToAdd.getQuantity());
            existingProduct.calculateAmount();
        } else {
            products.add(productToAdd);
            productToAdd.setInvoice(this);
        }

        calculateTotals();
    }
}
