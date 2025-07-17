package com.wornux.data.entity;

import jakarta.persistence.*;
import lombok.*;
import com.wornux.data.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@Builder
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Audited(withModifiedFlag = true)
@EqualsAndHashCode(exclude = "supplier")
@ToString(exclude = "supplier")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Min(0)
    @Column(name = "stock", nullable = false)
    private int stock;

    @Min(0)
    @Builder.Default
    @Column(name = "reorder_level", nullable = false)
    private int reorderLevel = 5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @JsonManagedReference
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ProductCategory category;
}