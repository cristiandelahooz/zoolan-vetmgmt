package com.wornux.services.interfaces;

import com.wornux.dto.dashboard.ChartDataDto;
import com.wornux.dto.dashboard.RevenueDataDto;
import com.wornux.dto.dashboard.StockAlertDto;

import java.util.List;
import java.util.Map;

public interface DashboardService {

  /**
   * Get revenue analysis with forecasting for the next 3 months
   */
  List<RevenueDataDto> getRevenueAnalysisWithForecast();

  /**
   * Get top performing services by revenue
   */
  List<ChartDataDto> getTopServicesAnalysis();

  /**
   * Get stock health analysis with alerts for low stock items
   */
  List<StockAlertDto> getStockHealthAnalysis();

  /**
   * Get consultation volume trends for staffing optimization
   */
  List<ChartDataDto> getConsultationTrends();

  /**
   * Get client retention metrics
   */
  Map<String, Object> getClientRetentionMetrics();

  /**
   * Get employee utilization data by hour of day
   */
  List<ChartDataDto> getEmployeeUtilizationData();
}