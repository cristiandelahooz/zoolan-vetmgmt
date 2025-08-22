package com.wornux.data.entity;

import com.wornux.data.enums.PaymentMethod;
import com.wornux.data.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@Entity
@Audited(withModifiedFlag = true)
@Table(name = "payments", indexes = { @Index(columnList = "paymentDate"), @Index(columnList = "method"),
        @Index(columnList = "status") })
public class Payment extends Auditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long code;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "payment")
    @ToString.Exclude
    private Set<PaymentDetail> details = new HashSet<>();

    @NotNull
    private LocalDate paymentDate;

    @NotNull
    private BigDecimal totalAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Size(max = 250)
    private String referenceNumber;

    @Size(max = 500)
    private String notes;

    public void addPaymentDetail(PaymentDetail detail) {
        details.add(detail);
    }

    public BigDecimal getTotalAllocated() {
        return details.stream().map(PaymentDetail::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

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
        Payment that = (Payment) o;
        return getCode() != null && Objects.equals(getCode(), that.getCode());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass().hashCode() : getClass().hashCode();
    }
}
