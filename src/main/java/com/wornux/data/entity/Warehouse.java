package com.wornux.data.entity;

import com.wornux.data.enums.WarehouseType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.Audited;

import java.util.List;

import static com.wornux.constants.ValidationConstants.MAX_WAREHOUSE_NAME_LENGTH;

@Entity
@Table(name = "warehouses")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Audited(withModifiedFlag = true)
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false, length = MAX_WAREHOUSE_NAME_LENGTH)
    @Size(min = 1, max = MAX_WAREHOUSE_NAME_LENGTH, message = "El nombre del almacén debe tener entre {min} y {max} caracteres")
    private String name;
    @Column(name = "warehouse_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private WarehouseType warehouseType;
    @Column(name = "status", nullable = false)
    @Builder.Default
    private boolean status = true;
    @Column(name = "available_for_sale")
    private boolean availableForSale;
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;
}
