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
@Table(name = "payments_detail")
public class PaymentDetail extends Auditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long code;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "payment", referencedColumnName = "code", nullable = false)
    private Payment payment;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "invoice", referencedColumnName = "code", nullable = false)
    private Invoice invoice;

    @NotNull
    private BigDecimal amount;

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer()
                .getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this)
                .getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass)
            return false;
        PaymentDetail that = (PaymentDetail) o;
        return getCode() != null && Objects.equals(getCode(), that.getCode());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass().hashCode() : getClass().hashCode();
    }
}
