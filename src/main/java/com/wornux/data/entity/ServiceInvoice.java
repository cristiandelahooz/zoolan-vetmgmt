package com.wornux.data.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.envers.Audited;

/** Composite entity linking invoices with services Similar to InvoiceProduct but for services */
@Entity
@Table(name = "invoice_services")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"invoice"})
@ToString(exclude = {"invoice"})
@Audited(withModifiedFlag = true)
public class ServiceInvoice {

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
}
