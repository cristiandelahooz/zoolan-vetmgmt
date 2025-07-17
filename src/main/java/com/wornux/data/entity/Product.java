package com.wornux.data.entity;

import jakarta.persistence.*;
import lombok.*;
import com.wornux.data.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.math.BigDecimal;

@Builder
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "supplier")
@ToString(exclude = "supplier")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private String description;

    @Builder.Default
    private boolean active = true;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    @Min(0)
    private int stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @JsonManagedReference
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;
}