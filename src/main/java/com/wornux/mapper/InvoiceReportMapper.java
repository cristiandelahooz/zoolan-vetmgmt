package com.wornux.mapper;

import com.wornux.data.entity.Invoice;
import com.wornux.dto.report.InvoiceReportDto;
import com.wornux.mapper.helper.InvoiceReportMapperHelper;
import org.mapstruct.*;

/**
 * Mapper para convertir entidades Invoice a DTOs de reporte. Utiliza MapStruct para generar código
 * en tiempo de compilación.
 */
@Mapper(
    componentModel = "spring",
    uses = {InvoiceReportMapperHelper.class})
public interface InvoiceReportMapper {

  @Mapping(target = "invoiceId", source = "code")
  @Mapping(target = "invoiceDate", source = "issuedDate")
  @Mapping(target = "clientEmail", source = "client.email")
  @Mapping(target = "clientName", expression = "java(invoice.getClient().getFullName())")
  @Mapping(target = "clientAddress", source = "client.streetAddress")
  @Mapping(target = "clientPhone", source = "client.phoneNumber")
  @Mapping(target = "totalAmount", qualifiedByName = "formatCurrency", source = "total")
  @Mapping(target = "subtotal", qualifiedByName = "formatCurrency", source = "subtotal")
  @Mapping(target = "tax", qualifiedByName = "formatCurrency", source = "tax")
  @Mapping(target = "notes", source = "notes")
  @Mapping(target = "salesOrder", source = "salesOrder")
  @Mapping(target = "productsData", qualifiedByName = "mapProductsToData", source = "products")
  InvoiceReportDto toReportDto(Invoice invoice);
}
