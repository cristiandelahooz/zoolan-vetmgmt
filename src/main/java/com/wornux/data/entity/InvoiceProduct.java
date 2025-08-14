package com.wornux.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@Entity
@Audited(withModifiedFlag = true)
@Table(name = "invoice_product")
public class InvoiceProduct extends Auditable implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long code;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice", referencedColumnName = "code", nullable = false)
  private Invoice invoice;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false)
  private Product product;

  @NotNull private Double quantity;

  @NotNull private BigDecimal price;

  @NotNull private BigDecimal amount;

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
    InvoiceProduct that = (InvoiceProduct) o;
    return getProduct() != null && Objects.equals(getProduct(), that.getProduct());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
