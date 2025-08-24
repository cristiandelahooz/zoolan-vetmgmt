package com.wornux.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.util.Objects;

/** Composite entity linking invoices with offerings Similar to InvoiceProduct but for offerings */
@Entity
@Table(name = "invoice_offerings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"invoice"})
@Audited(withModifiedFlag = true)
public class InvoiceOffering {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice", nullable = false)
  private Invoice invoice;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "offering", nullable = false)
  private Offering offering;

  @Builder.Default
  @Column(nullable = false)
  private Double quantity = 1.0;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  /** Calculate amount based on offering price and quantity */
  public void calculateAmount() {
    if (offering != null && offering.getPrice() != null && quantity != null) {
      this.amount = offering.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
  }

  /** Get offering price for convenience */
  public BigDecimal getPrice() {
    return offering != null ? offering.getPrice() : BigDecimal.ZERO;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass =
        o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    InvoiceOffering that = (InvoiceOffering) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
