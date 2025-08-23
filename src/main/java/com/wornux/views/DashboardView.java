package com.wornux.views;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardSection;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.dto.dashboard.ChartDataDto;
import com.wornux.dto.dashboard.RevenueDataDto;
import com.wornux.dto.dashboard.StockAlertDto;
import com.wornux.services.interfaces.DashboardService;
import jakarta.annotation.security.RolesAllowed;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RolesAllowed({"ROLE_SYSTEM_ADMIN", "ROLE_MANAGER", "ROLE_USER"})
@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard Ejecutivo")
public class DashboardView extends VerticalLayout {

  private final DashboardService dashboardService;
  private final NumberFormat currencyFormat;
  private final DateTimeFormatter dateFormatter;

  public DashboardView(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
    this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "DO"));
    this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    initializeDashboard();
    showStockAlerts();
  }

  private void initializeDashboard() {
    Dashboard dashboard = new Dashboard();
    dashboard.setMinimumColumnWidth("300px");
    dashboard.setMaximumColumnCount(3);

    DashboardSection financialSection = new DashboardSection("Análisis Financiero y Pronósticos");
    financialSection.add(createRevenueAnalysisWidget());
    financialSection.add(createTopServicesWidget());
    financialSection.add(createClientRetentionWidget());
    dashboard.addSection(financialSection);

    DashboardSection operationsSection = new DashboardSection("Control Operacional");
    operationsSection.add(createStockHealthWidget());
    operationsSection.add(createConsultationTrendsWidget());
    operationsSection.add(createEmployeeUtilizationWidget());
    dashboard.addSection(operationsSection);

    add(dashboard);
  }

  /**
   * Revenue Analysis with 3-month forecast
   * Helps manager plan cash flow and identify growth trends
   */
  private DashboardWidget createRevenueAnalysisWidget() {
    try {
      List<RevenueDataDto> revenueData = dashboardService.getRevenueAnalysisWithForecast();

      Chart chart = new Chart(ChartType.LINE);
      Configuration configuration = chart.getConfiguration();
      configuration.getTitle().setText("Análisis de Ingresos");

      ListSeries actualSeries = new ListSeries("Ingresos Reales");
      ListSeries forecastSeries = new ListSeries("Proyección");

      // Create and set plot options for forecast series
      PlotOptionsLine forecastPlotOptions = new PlotOptionsLine();
      forecastPlotOptions.setDashStyle(DashStyle.DASH);
      forecastSeries.setPlotOptions(forecastPlotOptions);

      for (RevenueDataDto data : revenueData) {
        if (data.getIsPrediction()) {
          forecastSeries.addData(data.getRevenue());
        } else {
          actualSeries.addData(data.getRevenue());
        }
      }

      configuration.addSeries(actualSeries);
      configuration.addSeries(forecastSeries);

      DashboardWidget widget = new DashboardWidget();
      widget.setTitle("Análisis de Ingresos");
      widget.setContent(chart);

      return widget;
    } catch (Exception e) {
      Notification.show("Error al cargar análisis de ingresos: " + e.getMessage(),
              3000, Notification.Position.TOP_CENTER)
          .addThemeVariants(NotificationVariant.LUMO_ERROR);

      DashboardWidget errorWidget = new DashboardWidget();
      errorWidget.setTitle("Análisis de Ingresos");
      errorWidget.setContent(new Span("Error al cargar datos"));
      return errorWidget;
    }
  }

  /**
   * Top performing services analysis
   * Helps identify most profitable services for resource allocation
   */
  private DashboardWidget createTopServicesWidget() {
    DashboardWidget widget = new DashboardWidget("Servicios Más Rentables");

    Div content = new Div();
    content.setClassName("dashboard-widget-content");

    Chart chart = new Chart(ChartType.COLUMN);
    chart.setMinHeight("350px");

    Configuration config = chart.getConfiguration();
    config.setTitle("Top 10 Servicios por Ingresos (6 meses)");
    config.getxAxis().setTitle("Servicios");
    config.getyAxis().setTitle("Ingresos (RD$)");

    List<ChartDataDto> servicesData = dashboardService.getTopServicesAnalysis();

    String[] categories = servicesData.stream()
        .map(ChartDataDto::getLabel)
        .toArray(String[]::new);

    Number[] values = servicesData.stream()
        .map(ChartDataDto::getValue)
        .toArray(Number[]::new);

    config.getxAxis().setCategories(categories);

    ListSeries series = new ListSeries("Ingresos");
    series.setData(values);
    config.addSeries(series);

    content.add(chart);
    widget.setContent(content);
    return widget;
  }

  /**
   * Stock health monitoring with predictive alerts
   * Critical for inventory management and preventing stockouts
   */
  private DashboardWidget createStockHealthWidget() {
    DashboardWidget widget = new DashboardWidget("Monitor de Inventario");

    Div content = new Div();
    content.setClassName("dashboard-widget-content");

    List<StockAlertDto> stockAlerts = dashboardService.getStockHealthAnalysis();

    long criticalCount = stockAlerts.stream()
        .mapToLong(alert -> "CRITICAL".equals(alert.getAlertLevel()) ? 1 : 0)
        .sum();

    long lowCount = stockAlerts.stream()
        .mapToLong(alert -> "LOW".equals(alert.getAlertLevel()) ? 1 : 0)
        .sum();

    long outOfStockCount = stockAlerts.stream()
        .mapToLong(alert -> "OUT_OF_STOCK".equals(alert.getAlertLevel()) ? 1 : 0)
        .sum();

    HorizontalLayout alertsLayout = new HorizontalLayout();
    alertsLayout.add(createAlertCard("Crítico", criticalCount, "error"));
    alertsLayout.add(createAlertCard("Bajo", lowCount, "warning"));
    alertsLayout.add(createAlertCard("Agotado", outOfStockCount, "error"));

    content.add(alertsLayout);

    if (!stockAlerts.isEmpty()) {
      VerticalLayout alertsList = new VerticalLayout();
      alertsList.setSpacing(false);

      stockAlerts.stream()
          .limit(5) // Show top 5 alerts
          .forEach(alert -> {
            Div alertDiv = new Div();
            alertDiv.addClassNames(
                LumoUtility.Padding.SMALL,
                LumoUtility.BorderRadius.MEDIUM,
                getAlertColorClass(alert.getAlertLevel())
            );

            String alertText = String.format("%s: %d unidades (mín: %d)",
                alert.getProductName(),
                alert.getCurrentStock(),
                alert.getMinimumStock());

            alertDiv.add(new Span(alertText));
            alertsList.add(alertDiv);
          });

      content.add(alertsList);
    }

    widget.setContent(content);
    return widget;
  }

  /**
   * Consultation trends for staffing optimization
   * Helps predict busy periods and optimize staff scheduling
   */
  private DashboardWidget createConsultationTrendsWidget() {
    DashboardWidget widget = new DashboardWidget("Tendencias de Consultas");

    Div content = new Div();
    content.setClassName("dashboard-widget-content");

    Chart chart = new Chart(ChartType.AREA);
    chart.setMinHeight("300px");

    Configuration config = chart.getConfiguration();
    config.setTitle("Volumen Diario de Consultas (3 meses)");
    config.getxAxis().setTitle("Fecha");
    config.getyAxis().setTitle("Número de Consultas");

    List<ChartDataDto> consultationData = dashboardService.getConsultationTrends();

    String[] categories = consultationData.stream()
        .map(data -> data.getDate().format(DateTimeFormatter.ofPattern("dd/MM")))
        .toArray(String[]::new);

    Number[] values = consultationData.stream()
        .map(ChartDataDto::getValue)
        .toArray(Number[]::new);

    config.getxAxis().setCategories(categories);

    ListSeries series = new ListSeries("Consultas");
    series.setData(values);
    config.addSeries(series);

    content.add(chart);
    widget.setContent(content);
    return widget;
  }

  /**
   * Client retention metrics
   * Critical for business growth and customer relationship management
   */
  private DashboardWidget createClientRetentionWidget() {
    DashboardWidget widget = new DashboardWidget("Retención de Clientes");

    Div content = new Div();
    content.setClassName("dashboard-widget-content");

    Map<String, Object> retentionData = dashboardService.getClientRetentionMetrics();

    Chart chart = new Chart(ChartType.PIE);
    chart.setMinHeight("300px");

    Configuration config = chart.getConfiguration();
    config.setTitle("Clientes: Nuevos vs Recurrentes");

    DataSeries series = new DataSeries();
    series.add(new DataSeriesItem("Nuevos", (Long) retentionData.get("newClients")));
    series.add(new DataSeriesItem("Recurrentes", (Long) retentionData.get("returningClients")));

    config.addSeries(series);

    double retentionRate = (Double) retentionData.get("retentionRate");
    HorizontalLayout kpiLayout = new HorizontalLayout();
    kpiLayout.add(createKpiCard("Tasa de Retención", String.format("%.1f%%", retentionRate), VaadinIcon.USERS));
    kpiLayout.add(createKpiCard("Clientes Activos", retentionData.get("totalActiveClients").toString(), VaadinIcon.USER_CHECK));

    content.add(kpiLayout, chart);
    widget.setContent(content);
    return widget;
  }

  /**
   * Employee utilization heatmap
   * Helps optimize staff scheduling based on consultation patterns
   */
  private DashboardWidget createEmployeeUtilizationWidget() {
    DashboardWidget widget = new DashboardWidget("Utilización de Personal");

    Div content = new Div();
    content.setClassName("dashboard-widget-content");

    Chart chart = new Chart(ChartType.COLUMN);
    chart.setMinHeight("300px");

    Configuration config = chart.getConfiguration();
    config.setTitle("Consultas por Hora del Día (4 semanas)");
    config.getxAxis().setTitle("Hora");
    config.getyAxis().setTitle("Promedio de Consultas");

    List<ChartDataDto> utilizationData = dashboardService.getEmployeeUtilizationData();

    String[] categories = utilizationData.stream()
        .map(ChartDataDto::getLabel)
        .toArray(String[]::new);

    Number[] values = utilizationData.stream()
        .map(ChartDataDto::getValue)
        .toArray(Number[]::new);

    config.getxAxis().setCategories(categories);

    ListSeries series = new ListSeries("Consultas");
    series.setData(values);
    config.addSeries(series);

    content.add(chart);
    widget.setContent(content);
    return widget;
  }

  private Div createKpiCard(String title, String value, VaadinIcon icon) {
    Div card = new Div();
    card.addClassNames(
        LumoUtility.Padding.MEDIUM,
        LumoUtility.BorderRadius.MEDIUM,
        LumoUtility.Background.CONTRAST_5
    );

    HorizontalLayout layout = new HorizontalLayout();
    layout.setAlignItems(Alignment.CENTER);

    Icon iconComponent = icon.create();
    iconComponent.addClassNames(LumoUtility.IconSize.LARGE, LumoUtility.TextColor.PRIMARY);

    VerticalLayout textLayout = new VerticalLayout();
    textLayout.setSpacing(false);
    textLayout.setPadding(false);

    Span titleSpan = new Span(title);
    titleSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

    H3 valueSpan = new H3(value);
    valueSpan.addClassNames(LumoUtility.Margin.NONE);

    textLayout.add(titleSpan, valueSpan);
    layout.add(iconComponent, textLayout);
    card.add(layout);

    return card;
  }

  private Div createAlertCard(String title, long count, String severity) {
    Div card = new Div();
    card.addClassNames(
        LumoUtility.Padding.MEDIUM,
        LumoUtility.BorderRadius.MEDIUM,
        "error".equals(severity) ? LumoUtility.Background.ERROR_10 : LumoUtility.Background.WARNING_10
    );

    VerticalLayout layout = new VerticalLayout();
    layout.setSpacing(false);
    layout.setPadding(false);

    Span titleSpan = new Span(title);
    titleSpan.addClassNames(LumoUtility.FontSize.SMALL);

    H3 countSpan = new H3(String.valueOf(count));
    countSpan.addClassNames(LumoUtility.Margin.NONE);

    layout.add(titleSpan, countSpan);
    card.add(layout);

    return card;
  }

  private String getAlertColorClass(String alertLevel) {
    return switch (alertLevel) {
      case "CRITICAL", "OUT_OF_STOCK" -> LumoUtility.Background.ERROR_10;
      case "LOW" -> LumoUtility.Background.WARNING_10;
      default -> LumoUtility.Background.CONTRAST_5;
    };
  }

  /**
   * Show critical stock alerts as notifications when dashboard loads
   * Provides immediate attention to urgent inventory issues
   */
  private void showStockAlerts() {
    List<StockAlertDto> criticalAlerts = dashboardService.getStockHealthAnalysis().stream()
        .filter(alert -> "CRITICAL".equals(alert.getAlertLevel()) || "OUT_OF_STOCK".equals(alert.getAlertLevel()))
        .limit(3)
        .toList();

    if (!criticalAlerts.isEmpty()) {
      StringBuilder message = new StringBuilder("Alertas críticas de inventario:\n");
      criticalAlerts.forEach(alert ->
          message.append(String.format("• %s: %d unidades restantes\n",
              alert.getProductName(), alert.getCurrentStock())));

      Notification notification = Notification.show(
          message.toString(),
          5000,
          Notification.Position.TOP_END
      );
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
  }
}