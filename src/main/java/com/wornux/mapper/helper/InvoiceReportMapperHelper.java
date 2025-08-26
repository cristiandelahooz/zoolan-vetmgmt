package com.wornux.mapper.helper;

import com.wornux.data.entity.Invoice;
import com.wornux.data.entity.InvoiceOffering;
import com.wornux.data.entity.InvoiceProduct;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Helper para InvoiceReportMapper que proporciona métodos de utilidad para el mapeo. Maneja
 * formateo de moneda y conversión de productos a estructura de datos para JasperReports.
 */
@Component
@RequiredArgsConstructor
public class InvoiceReportMapperHelper {

  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");

  /**
   * Formatea un valor BigDecimal como string de moneda.
   *
   * @param value El valor a formatear
   * @return String formateado con dos decimales
   */
  @Named("formatCurrency")
  public String formatCurrency(BigDecimal value) {
    if (value == null) {
      return "0.00";
    }
    return DECIMAL_FORMAT.format(value);
  }

  /**
   * Convierte un Set de InvoiceProduct a una lista de Maps para JasperReports.
   *
   * @param products Set de productos de la factura
   * @return Lista de Maps con los datos de cada producto
   */
  @Named("mapProductsToData")
  public List<Map<String, Object>> mapProductsToData(Set<InvoiceProduct> products) {
    if (products == null || products.isEmpty()) {
      return new ArrayList<>();
    }

    return products.stream().map(this::productToMap).toList();
  }

  @Named("mapAllItemsToData")
  public List<Map<String, Object>> mapAllItemsToData(Invoice invoice) {
    List<Map<String, Object>> allItems = new ArrayList<>();

    if (invoice.getProducts() != null && !invoice.getProducts().isEmpty()) {
      allItems.addAll(invoice.getProducts().stream().map(this::productToMap).toList());
    }

    if (invoice.getOfferings() != null && !invoice.getOfferings().isEmpty()) {
      allItems.addAll(invoice.getOfferings().stream().map(this::offeringToMap).toList());
    }

    return allItems;
  }

  /**
   * Convierte un InvoiceProduct individual a Map para JasperReports.
   *
   * @param product El producto a convertir
   * @return Map con los datos del producto
   */
  private Map<String, Object> productToMap(InvoiceProduct product) {
    Map<String, Object> map = new HashMap<>();

    map.put("productName", product.getProduct() != null ? product.getProduct().getName() : "");
    map.put(
        "description",
        product.getProduct() != null && product.getProduct().getDescription() != null
            ? product.getProduct().getDescription()
            : "");
    map.put("unitPrice", product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
    map.put("quantity", product.getQuantity() != null ? product.getQuantity() : 0.0);
    map.put("totalPrice", product.getAmount() != null ? product.getAmount() : BigDecimal.ZERO);

    return map;
  }

  @Named("mapOfferingsToData")
  public List<Map<String, Object>> mapOfferingsToData(List<InvoiceOffering> offerings) {
    if (offerings == null || offerings.isEmpty()) {
      return new ArrayList<>();
    }

    return offerings.stream().map(this::offeringToMap).toList();
  }

  private Map<String, Object> offeringToMap(InvoiceOffering offering) {
    Map<String, Object> map = new HashMap<>();

    map.put("productName", offering.getOffering() != null ? offering.getOffering().getName() : "");
    map.put(
        "description",
        offering.getOffering() != null && offering.getOffering().getDescription() != null
            ? offering.getOffering().getDescription()
            : "");
    map.put("unitPrice", offering.getPrice() != null ? offering.getPrice() : BigDecimal.ZERO);
    map.put("quantity", offering.getQuantity() != null ? offering.getQuantity() : 0.0);
    map.put("totalPrice", offering.getAmount() != null ? offering.getAmount() : BigDecimal.ZERO);

    return map;
  }
}
