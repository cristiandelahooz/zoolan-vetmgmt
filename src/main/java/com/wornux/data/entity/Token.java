package com.wornux.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@Entity
@Table(name = "tokens", indexes = { @Index(columnList = "username"), @Index(columnList = "accessToken"),
        @Index(columnList = "refreshToken") })
public class Token implements Serializable {

    @Id
    private String code;

    @NotNull
    @NotEmpty
    @Size(min = 1, max = 50)
    private String username;

    @NotNull
    @NotEmpty
    @Size(min = 1, max = 9999)
    private String accessToken;

    @NotNull
    @NotEmpty
    @Size(min = 1, max = 9999)
    private String refreshToken;

    private boolean invalidated;
    private boolean suspended;

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
        Token token = (Token) o;
        return getCode() != null && Objects.equals(getCode(), token.getCode());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass().hashCode() : getClass().hashCode();
    }
}
