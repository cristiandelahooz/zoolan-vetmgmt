package com.wornux.data.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.wornux.data.enums.ProductCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.envers.Audited;

@Builder
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited(withModifiedFlag = true)
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
    @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "sales_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal salesPrice;

    @Min(0)
    @Column(name = "accounting_stock", nullable = false)
    private int accountingStock;

    @Min(0)
    @Column(name = "available_stock", nullable = false)
    private int availableStock;

    @Min(0)
    @Builder.Default
    @Column(name = "reorder_level", nullable = false)
    private int reorderLevel = 5;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier")
    @JsonManagedReference
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ProductCategory category;

    @ManyToOne
    @JoinColumn(name = "warehouse")
    private Warehouse warehouse;
}
