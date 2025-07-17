package com.wornux.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierListDto {
    private Long id;
    private String rnc;
    private String companyName;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String province;
    private String municipality;
    private String sector;
    private String streetAddress;
    private boolean active;

}