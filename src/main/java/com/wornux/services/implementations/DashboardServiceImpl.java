package com.wornux.services.implementations;

import com.wornux.data.repository.ClientRepository;
import com.wornux.data.repository.ConsultationRepository;
import com.wornux.data.repository.InvoiceRepository;
import com.wornux.data.repository.ProductRepository;
import com.wornux.data.repository.ServiceRepository;
import com.wornux.dto.dashboard.ChartDataDto;
import com.wornux.dto.dashboard.RevenueDataDto;
import com.wornux.dto.dashboard.StockAlertDto;
import com.wornux.services.interfaces.DashboardService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

  private final InvoiceRepository invoiceRepository;
  private final ProductRepository productRepository;
  private final ServiceRepository serviceRepository;
  private final ConsultationRepository consultationRepository;
  private final ClientRepository clientRepository;

  @Override
  public List<RevenueDataDto> getRevenueAnalysisWithForecast() {
    LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
    LocalDateTime now = LocalDateTime.now();

    List<Object[]> results =
        invoiceRepository.findMonthlyRevenue(
            sixMonthsAgo.atZone(ZoneId.systemDefault()).toInstant(),
            now.atZone(ZoneId.systemDefault()).toInstant());
    List<RevenueDataDto> revenueData = new ArrayList<>();

    for (Object[] row : results) {
      LocalDate period = ((java.sql.Date) row[0]).toLocalDate();
      Double revenue = ((Number) row[1]).doubleValue();
      Long invoiceCount = ((Number) row[2]).longValue();

      revenueData.add(new RevenueDataDto(period, revenue, invoiceCount, false));
    }

    // Add simple forecast for next 3 months (basic average)
    if (!revenueData.isEmpty()) {
      double avgRevenue =
          revenueData.stream().mapToDouble(RevenueDataDto::getRevenue).average().orElse(0.0);

      LocalDate lastPeriod = revenueData.get(revenueData.size() - 1).getPeriod();
      for (int i = 1; i <= 3; i++) {
        LocalDate forecastPeriod = lastPeriod.plusMonths(i);
        revenueData.add(new RevenueDataDto(forecastPeriod, avgRevenue, 0L, true));
      }
    }

    return revenueData;
  }

  @Override
  public List<ChartDataDto> getTopServicesAnalysis() {
    LocalDateTime startDate = LocalDateTime.now().minusMonths(6);
    Pageable topTen = PageRequest.of(0, 10);

    Instant startDateInstant = startDate.atZone(ZoneId.systemDefault()).toInstant();
    List<Object[]> servicesData =
        invoiceRepository.findTopServicesByRevenue(startDateInstant, topTen);

    return servicesData.stream()
        .map(
            data ->
                new ChartDataDto((String) data[0], ((Number) data[1]).doubleValue(), null, null))
        .collect(Collectors.toList());
  }

  @Override
  public List<StockAlertDto> getStockHealthAnalysis() {
    List<Object[]> results = productRepository.findProductStockLevels();

    return results.stream()
        .map(
            row -> {
              Long productId = ((Number) row[0]).longValue();
              String productName = (String) row[1];
              Integer accountingStock = ((Number) row[2]).intValue();
              Integer availableStock = ((Number) row[3]).intValue();
              String category = row[4].toString();

              String alertLevel;
              if (availableStock == 0) {
                alertLevel = "OUT_OF_STOCK";
              } else if (availableStock <= 5) {
                alertLevel = "CRITICAL";
              } else if (availableStock <= 10) {
                alertLevel = "LOW";
              } else {
                alertLevel = "NORMAL";
              }

              return new StockAlertDto(
                  productId,
                  productName,
                  availableStock,
                  5,
                  category,
                  alertLevel); // Using 5 as minimum stock
            })
        .filter(alert -> !"NORMAL".equals(alert.getAlertLevel()))
        .collect(Collectors.toList());
  }

  @Override
  public List<ChartDataDto> getConsultationTrends() {
    LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
    LocalDateTime now = LocalDateTime.now();

    List<Object[]> results =
        consultationRepository.findDailyConsultationCounts(threeMonthsAgo, now);

    return results.stream()
        .map(
            row -> {
              LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
              Double count = ((Number) row[1]).doubleValue();

              return new ChartDataDto(date.toString(), count, date, null);
            })
        .collect(Collectors.toList());
  }

  @Override
  public Map<String, Object> getClientRetentionMetrics() {
    LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
    LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

    // Convert to Instant for InvoiceRepository (which expects Instant)
    Instant sixMonthsAgoInstant = sixMonthsAgo.atZone(ZoneId.systemDefault()).toInstant();

    // Use LocalDateTime for ClientRepository (which expects LocalDateTime)
    Long newClients =
        clientRepository.countNewClientsByPeriod(
            threeMonthsAgo.atZone(ZoneId.systemDefault()).toInstant(),
            LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
    Long returningClients =
        clientRepository.countReturningClientsByPeriod(
            threeMonthsAgo.atZone(ZoneId.systemDefault()).toInstant(),
            LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
    Long totalActiveClients = clientRepository.countActiveClients();

    double retentionRate = 0;
    if (totalActiveClients > 0) {
      retentionRate = (returningClients.doubleValue() / totalActiveClients.doubleValue()) * 100;
    }

    Map<String, Object> metrics = new HashMap<>();
    metrics.put("newClients", newClients);
    metrics.put("returningClients", returningClients);
    metrics.put("totalActiveClients", totalActiveClients);
    metrics.put("retentionRate", retentionRate);

    return metrics;
  }

  @Override
  public List<ChartDataDto> getEmployeeUtilizationData() {
    LocalDateTime fourWeeksAgo = LocalDateTime.now().minusWeeks(4);
    LocalDateTime now = LocalDateTime.now();

    List<Object[]> results =
        consultationRepository.findEmployeeUtilizationByHour(fourWeeksAgo, now);

    return results.stream()
        .map(
            row -> {
              Integer hour = ((Number) row[0]).intValue();
              Double count = ((Number) row[1]).doubleValue();

              return new ChartDataDto(hour + ":00", count, null, null);
            })
        .collect(Collectors.toList());
  }
}
