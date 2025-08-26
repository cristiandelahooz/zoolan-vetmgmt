package com.wornux.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.charts.model.style.FontWeight;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Product;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.SystemRole;
import com.wornux.dto.dashboard.ChartDataDto;
import com.wornux.dto.dashboard.RevenueDataDto;
import com.wornux.dto.dashboard.StockAlertDto;
import com.wornux.security.UserUtils;
import com.wornux.services.interfaces.DashboardService;
import com.wornux.services.interfaces.ProductService;
import com.wornux.services.interfaces.SupplierService;
import com.wornux.services.interfaces.WarehouseService;
import com.wornux.views.calendar.AppointmentCalendarView;
import com.wornux.views.products.ProductForm;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

//@RolesAllowed({"ROLE_SYSTEM_ADMIN", "ROLE_MANAGER"})
@PermitAll
@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard Ejecutivo")
//public class DashboardView extends VerticalLayout {
public class DashboardView extends VerticalLayout  {


  private final DashboardService dashboardService;
  private final ProductService productService;
  private final SupplierService supplierService;
  private final WarehouseService warehouseService;
  private final NumberFormat currencyFormat;
  private final DateTimeFormatter dateFormatter;
  //private final ProductForm productForm;
  private ProductForm productForm;

  // Paleta de colores moderna
  private final String[] modernColors = {
    "#10B981", "#3B82F6", "#8B5CF6", "#F59E0B",
    "#EF4444", "#06B6D4", "#84CC16", "#F97316",
    "#EC4899", "#6366F1", "#14B8A6", "#F43F5E"
  };
  private Grid<StockAlertDto> alertsGrid; // New field
  private Product selectedProduct;
  private boolean dashboardBuilt = false;

  public DashboardView(
      DashboardService dashboardService,
      @Qualifier("productServiceImpl") ProductService productService,
      @Qualifier("supplierServiceImpl") SupplierService supplierService,
      @Qualifier("warehouseServiceImpl") WarehouseService warehouseService) {
    this.dashboardService = dashboardService;
    this.productService = productService;
    this.supplierService = supplierService;
    this.warehouseService = warehouseService;
    this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "DO"));
    this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /*if (isAdminOrManager()) {
    this.productForm = new ProductForm(productService, supplierService, warehouseService);
    add(productForm);
    productForm.setOnSaveCallback(this::refreshStockAlertsGrid);

    initializeDashboard();
    showStockAlerts();
    } else
    {
      buildNonAdminHero();

    }*/
  }

  @Override
  protected void onAttach(AttachEvent e) {
    if (!isAdminOrManager()) {
      e.getUI().navigate(AppointmentCalendarView.class);           // o AppointmentsView.class
      return;
    }
    buildAdminDashboard(); // tu initializeDashboard() + showStockAlerts()
  }

  private void buildAdminDashboard() {
    if (dashboardBuilt) return;          // evita construir dos veces

    removeAll();                         // limpia cualquier hero previo

    if (productForm == null) {           // crea el form solo una vez
      productForm = new ProductForm(productService, supplierService, warehouseService);
      productForm.setOnSaveCallback(this::refreshStockAlertsGrid);
    }

    add(productForm);                    // arriba del dashboard (como tenías)
    initializeDashboard();               // tabs + widgets
    showStockAlerts();                   // notificaciones críticas

    dashboardBuilt = true;
  }


  private void initializeDashboard() {
    Tab financialTab = new Tab("Análisis Financiero");
    Tab operationsTab = new Tab(" Control Operacional");
    Tabs tabs = new Tabs(financialTab, operationsTab);

    // Styling moderno para las tabs
    tabs.getStyle()
        .set("background", "white")
        .set("border-radius", "12px")
        .set("box-shadow", "0 4px 16px rgba(0,0,0,0.08)")
        .set("padding", "8px");

    HorizontalLayout centeredTabsLayout = new HorizontalLayout(tabs);
    centeredTabsLayout.setWidthFull();
    centeredTabsLayout.setJustifyContentMode(JustifyContentMode.CENTER);
    centeredTabsLayout.getStyle().set("margin-bottom", "var(--lumo-space-l)");

    VerticalLayout financialContent = new VerticalLayout();
    financialContent.add(createModernRevenueAnalysisWidget());
    financialContent.add(createModernTopServicesWidget());
    financialContent.add(createModernClientRetentionWidget());
    financialContent.setSizeFull();
    financialContent.setVisible(true);

    VerticalLayout operationsContent = new VerticalLayout();
    operationsContent.add(createModernStockHealthWidget());
    operationsContent.add(createModernConsultationTrendsWidget());
    operationsContent.add(createModernEmployeeUtilizationWidget());
    operationsContent.setSizeFull();
    operationsContent.setVisible(false);

    tabs.addSelectedChangeListener(
        event -> {
          financialContent.setVisible(tabs.getSelectedTab() == financialTab);
          operationsContent.setVisible(tabs.getSelectedTab() == operationsTab);
        });

    add(centeredTabsLayout, financialContent, operationsContent);
  }

  /** Modern Revenue Analysis with enhanced visual design */
  private DashboardWidget createModernRevenueAnalysisWidget() {
    try {
      DashboardWidget widget = new DashboardWidget("Análisis de Ingresos");

      Div content = new Div();
      content.addClassName("modern-chart-widget");
      content
          .getStyle()
          .set("background", "linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)")
          .set("border-radius", "16px")
          .set("padding", "var(--lumo-space-l)")
          .set("box-shadow", "0 8px 32px rgba(0,0,0,0.1)");

      List<RevenueDataDto> revenueData = dashboardService.getRevenueAnalysisWithForecast();

      // KPIs Header
      HorizontalLayout kpiHeader = createRevenueKPIs(revenueData);

      Chart chart = new Chart(ChartType.AREASPLINE);
      chart.setHeight("400px");
      chart
          .getStyle()
          .set("background", "white")
          .set("border-radius", "12px")
          .set("box-shadow", "0 4px 16px rgba(0,0,0,0.05)");

      Configuration config = chart.getConfiguration();
      config.setTitle("Tendencia de Ingresos con Proyección");

      // Styling del título
      config.getTitle().getStyle().setFontSize("18px");
      config.getTitle().getStyle().setFontWeight(FontWeight.BOLD);
      config.getTitle().getStyle().setColor(new SolidColor("#1f2937"));

      config.getxAxis().setTitle("Período");
      config.getyAxis().setTitle("Ingresos (RD$)");

      // Grid y styling
      config.getxAxis().setGridLineWidth(1);
      config.getxAxis().setGridLineColor(new SolidColor("#f1f5f9"));
      config.getyAxis().setGridLineWidth(1);
      config.getxAxis().setGridLineColor(new SolidColor("#f1f5f9"));

      String[] categories =
          revenueData.stream()
              .map(data -> data.getPeriod().format(dateFormatter))
              .toArray(String[]::new);

      Number[] values = revenueData.stream().map(RevenueDataDto::getRevenue).toArray(Number[]::new);

      config.getxAxis().setCategories(categories);

      ListSeries series = new ListSeries("Ingresos Mensuales");
      series.setData(values);

      // Styling de la serie
      PlotOptionsAreaspline plotOptions = new PlotOptionsAreaspline();
      plotOptions.setColor(new SolidColor("#10B981"));
      plotOptions.setFillColor(new SolidColor("#10B981"));
      plotOptions.getMarker().setEnabled(true);
      plotOptions.getMarker().setRadius(6);
      plotOptions.getMarker().setSymbol(MarkerSymbolEnum.CIRCLE);
      plotOptions.getMarker().setFillColor(new SolidColor("#059669"));
      plotOptions.getMarker().setLineColor(new SolidColor("white"));
      plotOptions.getMarker().setLineWidth(2);

      series.setPlotOptions(plotOptions);
      config.addSeries(series);

      // Tooltip moderno
      Tooltip tooltip = config.getTooltip();
      tooltip.setFormatter(
          "function() { return '<b>' + this.x + '</b><br/>' + this.series.name + ': ' + Highcharts.numberFormat(this.y, 0, '.', ',') + ' RD$'; }");
      tooltip.setBackgroundColor(new SolidColor("#1f2937"));
      tooltip.getStyle().setColor(new SolidColor("white"));

      content.add(kpiHeader, chart);
      widget.setContent(content);
      widget.setClassName(LumoUtility.Width.FULL);
      return widget;

    } catch (Exception e) {
      Notification.show("Error cargando análisis de ingresos", 3000, Notification.Position.TOP_END)
          .addThemeVariants(NotificationVariant.LUMO_ERROR);
      return new DashboardWidget("Error en Revenue Analysis");
    }
  }

  private HorizontalLayout createRevenueKPIs(List<RevenueDataDto> revenueData) {
    HorizontalLayout kpiLayout = new HorizontalLayout();
    kpiLayout.setWidthFull();
    kpiLayout.setJustifyContentMode(JustifyContentMode.AROUND);
    kpiLayout.getStyle().set("margin-bottom", "var(--lumo-space-l)");

    if (!revenueData.isEmpty()) {
      double totalRevenue =
          revenueData.stream().mapToDouble(r -> r.getRevenue().doubleValue()).sum();

      double avgRevenue = totalRevenue / revenueData.size();

      // Calcular crecimiento (simulado)
      double growth = 12.5;

      Div totalCard =
          createModernKPICard(
              currencyFormat.format(totalRevenue),
              "Ingresos Totales",
              VaadinIcon.DOLLAR.create(),
              "#10B981");

      Div avgCard =
          createModernKPICard(
              currencyFormat.format(avgRevenue),
              "Promedio Mensual",
              VaadinIcon.CHART.create(),
              "#3B82F6");

      Div growthCard =
          createModernKPICard(
              String.format("%.1f%%", growth),
              "Crecimiento",
              VaadinIcon.TRENDING_UP.create(),
              "#059669");

      kpiLayout.add(totalCard, avgCard, growthCard);
    }

    return kpiLayout;
  }

  /** Modern Top Services with enhanced visual design */
  private DashboardWidget createModernTopServicesWidget() {
    DashboardWidget widget = new DashboardWidget("Servicios Estrella");

    Div content = new Div();
    content.addClassName("modern-chart-widget");
    content
        .getStyle()
        .set("background", "linear-gradient(135deg, #fefbff 0%, #f3e8ff 100%)")
        .set("border-radius", "16px")
        .set("padding", "var(--lumo-space-l)")
        .set("box-shadow", "0 8px 32px rgba(139, 92, 246, 0.1)");

    Chart chart = new Chart(ChartType.COLUMN);
    chart.setHeight("400px");
    chart
        .getStyle()
        .set("background", "white")
        .set("border-radius", "12px")
        .set("box-shadow", "0 4px 16px rgba(0,0,0,0.05)");

    Configuration config = chart.getConfiguration();
    config.setTitle("Top 10 Servicios Más Rentables");

    config.getTitle().getStyle().setFontSize("18px");
    config.getTitle().getStyle().setFontWeight(FontWeight.BOLD);
    config.getTitle().getStyle().setColor(new SolidColor("#1f2937"));

    config.getxAxis().setTitle("Servicios");
    config.getyAxis().setTitle("Ingresos (RD$)");

    // Grid styling
    config.getxAxis().setGridLineWidth(1);
    config.getxAxis().setGridLineColor(new SolidColor("#f8fafc"));
    config.getyAxis().setGridLineWidth(1);
    config.getyAxis().setGridLineColor(new SolidColor("#f8fafc"));

    List<ChartDataDto> servicesData = dashboardService.getTopServicesAnalysis();

    String[] categories = servicesData.stream().map(ChartDataDto::getLabel).toArray(String[]::new);

    Number[] values = servicesData.stream().map(ChartDataDto::getValue).toArray(Number[]::new);

    config.getxAxis().setCategories(categories);

    ListSeries series = new ListSeries("Ingresos por Servicio");
    series.setData(values);

    // Styling moderno para las columnas
    PlotOptionsColumn plotOptions = new PlotOptionsColumn();

    // Gradiente para las columnas
    String[] columnColors = {
      "#8B5CF6", "#7C3AED", "#6D28D9", "#5B21B6", "#4C1D95", "#3B1F87", "#312E81", "#2D1B69"
    };

    for (int i = 0; i < Math.min(values.length, columnColors.length); i++) {
      plotOptions.setColor(new SolidColor(columnColors[i % columnColors.length]));
    }

    plotOptions.setBorderRadius(8);
    plotOptions.getDataLabels().setEnabled(true);
    plotOptions.getDataLabels().setFormat("{y:,.0f} RD$");
    plotOptions.getDataLabels().getStyle().setColor(new SolidColor("#1f2937"));
    plotOptions.getDataLabels().getStyle().setFontWeight(FontWeight.BOLD);

    series.setPlotOptions(plotOptions);
    config.addSeries(series);

    // Tooltip moderno
    Tooltip tooltip = config.getTooltip();
    tooltip.setFormatter(
        "function() { return '<b>' + this.x + '</b><br/>' + 'Ingresos: ' + Highcharts.numberFormat(this.y, 0, '.', ',') + ' RD$'; }");
    tooltip.setBackgroundColor(new SolidColor("#1f2937"));
    tooltip.getStyle().setColor(new SolidColor("white"));

    content.add(chart);
    widget.setContent(content);
    widget.setClassName(LumoUtility.Width.FULL);
    return widget;
  }

  /** Modern Client Retention with enhanced visual design */
  private DashboardWidget createModernClientRetentionWidget() {
    DashboardWidget widget = new DashboardWidget("Lealtad de Clientes");

    Div content = new Div();
    content.addClassName("modern-chart-widget");
    content
        .getStyle()
        .set("background", "linear-gradient(135deg, #f0fdfa 0%, #ccfbf1 100%)")
        .set("border-radius", "16px")
        .set("padding", "var(--lumo-space-l)")
        .set("box-shadow", "0 8px 32px rgba(16, 185, 129, 0.1)");

    Map<String, Object> retentionData = dashboardService.getClientRetentionMetrics();

    // KPIs Header
    HorizontalLayout retentionKPIs = createRetentionKPIs(retentionData);

    Chart chart = new Chart(ChartType.PIE);
    chart.setHeight("350px");
    chart
        .getStyle()
        .set("background", "white")
        .set("border-radius", "12px")
        .set("box-shadow", "0 4px 16px rgba(0,0,0,0.05)");

    Configuration config = chart.getConfiguration();
    config.setTitle("Distribución de Clientes");

    config.getTitle().getStyle().setFontSize("18px");
    config.getTitle().getStyle().setFontWeight(FontWeight.BOLD);
    config.getTitle().getStyle().setColor(new SolidColor("#1f2937"));

    DataSeries series = new DataSeries();

    DataSeriesItem newItem = new DataSeriesItem("Nuevos", (Long) retentionData.get("newClients"));
    newItem.setColor(new SolidColor("#3B82F6"));

    DataSeriesItem returningItem =
        new DataSeriesItem("Recurrentes", (Long) retentionData.get("returningClients"));
    returningItem.setColor(new SolidColor("#10B981"));

    series.add(newItem);
    series.add(returningItem);
    // Styling del donut
    PlotOptionsPie plotOptions = new PlotOptionsPie();
    plotOptions.setInnerSize("60%");
    plotOptions.getDataLabels().setEnabled(true);
    plotOptions.getDataLabels().setFormat("{point.name}: <b>{point.percentage:.1f}%</b>");
    plotOptions.getDataLabels().getStyle().setFontWeight(FontWeight.BOLD);
    plotOptions.getDataLabels().getStyle().setColor(new SolidColor("#1f2937"));

    series.setPlotOptions(plotOptions);
    config.addSeries(series);

    // Tooltip moderno
    Tooltip tooltip = config.getTooltip();
    tooltip.setPointFormat("<b>{point.percentage:.1f}%</b><br/> Clientes: {point.y}");
    tooltip.setBackgroundColor(new SolidColor("#1f2937"));

    tooltip.getStyle().setColor(new SolidColor("white"));

    content.add(retentionKPIs, chart);
    widget.setContent(content);
    widget.setClassName(LumoUtility.Width.FULL);
    return widget;
  }

  private HorizontalLayout createRetentionKPIs(Map<String, Object> retentionData) {
    HorizontalLayout kpiLayout = new HorizontalLayout();
    kpiLayout.setWidthFull();
    kpiLayout.setJustifyContentMode(JustifyContentMode.AROUND);
    kpiLayout.getStyle().set("margin-bottom", "var(--lumo-space-l)");

    double retentionRate = (Double) retentionData.get("retentionRate");

    Div retentionCard =
        createModernKPICard(
            String.format("%.1f%%", retentionRate),
            "Tasa de Retención",
            VaadinIcon.HEART.create(),
            "#10B981");

    Div newClientsCard =
        createModernKPICard(
            retentionData.get("newClients").toString(),
            "Clientes Nuevos",
            VaadinIcon.USERS.create(),
            "#3B82F6");

    Div returningCard =
        createModernKPICard(
            retentionData.get("returningClients").toString(),
            "Clientes Leales",
            VaadinIcon.REFRESH.create(),
            "#059669");

    kpiLayout.add(retentionCard, newClientsCard, returningCard);
    return kpiLayout;
  }

  /** Modern Consultation Trends with enhanced visual design */
  private DashboardWidget createModernConsultationTrendsWidget() {
    DashboardWidget widget = new DashboardWidget("Tendencias de Consultas");

    Div content = new Div();
    content.addClassName("modern-chart-widget");
    content
        .getStyle()
        .set("background", "linear-gradient(135deg, #fefce8 0%, #fef3c7 100%)")
        .set("border-radius", "16px")
        .set("padding", "var(--lumo-space-l)")
        .set("box-shadow", "0 8px 32px rgba(245, 158, 11, 0.1)");

    Chart chart = new Chart(ChartType.SPLINE);
    chart.setHeight("350px");
    chart
        .getStyle()
        .set("background", "white")
        .set("border-radius", "12px")
        .set("box-shadow", "0 4px 16px rgba(0,0,0,0.05)");

    Configuration config = chart.getConfiguration();
    config.setTitle("Volumen de Consultas por Período");

    config.getTitle().getStyle().setFontSize("18px");
    config.getTitle().getStyle().setFontWeight(FontWeight.BOLD);
    config.getTitle().getStyle().setColor(new SolidColor("#1f2937"));

    config.getxAxis().setTitle("Fecha");
    config.getyAxis().setTitle("Número de Consultas");

    // Grid styling
    config.getxAxis().setGridLineWidth(1);
    config.getxAxis().setGridLineColor(new SolidColor("#fef9c3"));
    config.getyAxis().setGridLineWidth(1);
    config.getyAxis().setGridLineColor(new SolidColor("#fef9c3"));

    List<ChartDataDto> consultationData = dashboardService.getConsultationTrends();

    String[] categories =
        consultationData.stream().map(ChartDataDto::getLabel).toArray(String[]::new);

    Number[] values = consultationData.stream().map(ChartDataDto::getValue).toArray(Number[]::new);

    config.getxAxis().setCategories(categories);

    ListSeries series = new ListSeries("Consultas Diarias");
    series.setData(values);

    // Styling moderno para la línea
    PlotOptionsSpline plotOptions = new PlotOptionsSpline();
    plotOptions.setColor(new SolidColor("#F59E0B"));
    plotOptions.setLineWidth(3);
    plotOptions.getMarker().setEnabled(true);
    plotOptions.getMarker().setRadius(5);
    plotOptions.getMarker().setSymbol(MarkerSymbolEnum.CIRCLE);
    plotOptions.getMarker().setFillColor(new SolidColor("#D97706"));
    plotOptions.getMarker().setLineColor(new SolidColor("white"));
    plotOptions.getMarker().setLineWidth(2);

    series.setPlotOptions(plotOptions);
    config.addSeries(series);

    // Tooltip moderno
    Tooltip tooltip = config.getTooltip();
    tooltip.setFormatter(
        "function() { return '<b>' + this.x + '</b><br/>' + 'Consultas: ' + this.y; }");
    tooltip.setBackgroundColor(new SolidColor("#1f2937"));
    tooltip.getStyle().setColor(new SolidColor("white"));

    content.add(chart);
    widget.setContent(content);
    widget.setClassName(LumoUtility.Width.FULL);
    return widget;
  }

  /** Modern Employee Utilization with enhanced visual design */
  private DashboardWidget createModernEmployeeUtilizationWidget() {
    DashboardWidget widget = new DashboardWidget("👨‍⚕️ Utilización de Personal");

    Div content = new Div();
    content.addClassName("modern-chart-widget");
    content
        .getStyle()
        .set("background", "linear-gradient(135deg, #fdf2f8 0%, #fce7f3 100%)")
        .set("border-radius", "16px")
        .set("padding", "var(--lumo-space-l)")
        .set("box-shadow", "0 8px 32px rgba(236, 72, 153, 0.1)");

    Chart chart = new Chart(ChartType.AREASPLINE);
    chart.setHeight("350px");
    chart
        .getStyle()
        .set("background", "white")
        .set("border-radius", "12px")
        .set("box-shadow", "0 4px 16px rgba(0,0,0,0.05)");

    Configuration config = chart.getConfiguration();
    config.setTitle("⏰ Distribución de Carga de Trabajo");

    config.getTitle().getStyle().setFontSize("18px");
    config.getTitle().getStyle().setFontWeight(FontWeight.BOLD);
    config.getTitle().getStyle().setColor(new SolidColor("#1f2937"));

    config.getxAxis().setTitle("🕐 Hora del Día");
    config.getyAxis().setTitle("Promedio de Consultas");

    // Grid styling
    config.getxAxis().setGridLineWidth(1);
    config.getxAxis().setGridLineColor(new SolidColor("#fdf2f8"));
    config.getyAxis().setGridLineWidth(1);
    config.getyAxis().setGridLineColor(new SolidColor("#fdf2f8"));

    List<ChartDataDto> utilizationData = dashboardService.getEmployeeUtilizationData();

    String[] categories =
        utilizationData.stream().map(ChartDataDto::getLabel).toArray(String[]::new);

    Number[] values = utilizationData.stream().map(ChartDataDto::getValue).toArray(Number[]::new);

    config.getxAxis().setCategories(categories);

    ListSeries series = new ListSeries("⚡ Carga de Trabajo");
    series.setData(values);

    // Styling moderno para el área
    PlotOptionsAreaspline plotOptions = new PlotOptionsAreaspline();
    plotOptions.setColor(new SolidColor("#EC4899"));
    plotOptions.setFillColor(new SolidColor("#EC4899"));
    plotOptions.getMarker().setEnabled(true);
    plotOptions.getMarker().setRadius(5);
    plotOptions.getMarker().setSymbol(MarkerSymbolEnum.CIRCLE);
    plotOptions.getMarker().setFillColor(new SolidColor("#BE185D"));
    plotOptions.getMarker().setLineColor(new SolidColor("white"));
    plotOptions.getMarker().setLineWidth(2);

    series.setPlotOptions(plotOptions);
    config.addSeries(series);

    // Tooltip moderno
    Tooltip tooltip = config.getTooltip();
    tooltip.setFormatter(
        "function() { return '<b>' + this.x + '</b><br/>' + 'Consultas promedio: ' + this.y; }");
    tooltip.setBackgroundColor(new SolidColor("#1f2937"));
    tooltip.getStyle().setColor(new SolidColor("white"));

    content.add(chart);
    widget.setContent(content);
    widget.setClassName(LumoUtility.Width.FULL);
    return widget;
  }

  // [Resto de métodos del widget de stock permanecen igual...]
  private DashboardWidget createModernStockHealthWidget() {
    DashboardWidget widget = new DashboardWidget("Centro de Control de Inventario");

    Div content = new Div();
    content.addClassName("modern-stock-widget");
    content
        .getStyle()
        .set(
            "background",
            "linear-gradient(135deg, var(--lumo-contrast-5pct) 0%, var(--lumo-primary-color-10pct) 100%)")
        .set("border-radius", "16px")
        .set("padding", "var(--lumo-space-l)");

    List<StockAlertDto> stockAlerts = dashboardService.getStockHealthAnalysis();

    HorizontalLayout metricsRow = createAdvancedMetricsRow(stockAlerts);
    Div alertsPanel = createInteractiveAlertsPanel(stockAlerts);
    HorizontalLayout analyticsSection = createAdvancedAnalyticsSection(stockAlerts);

    VerticalLayout mainLayout = new VerticalLayout();
    mainLayout.setPadding(false);
    mainLayout.add(metricsRow, alertsPanel, analyticsSection);

    content.add(mainLayout);
    widget.setContent(content);
    widget.setClassName(LumoUtility.Width.FULL);

    return widget;
  }

  private HorizontalLayout createAdvancedMetricsRow(List<StockAlertDto> stockAlerts) {
    HorizontalLayout metricsRow = new HorizontalLayout();
    metricsRow.setWidthFull();
    metricsRow.setJustifyContentMode(JustifyContentMode.AROUND);
    metricsRow.addClassName("metrics-row");

    // Calcular métricas
    long criticalCount =
        stockAlerts.stream().filter(alert -> "CRITICAL".equals(alert.getAlertLevel())).count();

    long lowCount =
        stockAlerts.stream().filter(alert -> "LOW".equals(alert.getAlertLevel())).count();

    long outOfStockCount =
        stockAlerts.stream().filter(alert -> "OUT_OF_STOCK".equals(alert.getAlertLevel())).count();

    long healthyCount = dashboardService.getHealthyStockCount();
    long totalProducts = criticalCount + lowCount + outOfStockCount + healthyCount;

    // Métricas con animaciones y progreso
    Div healthMetric =
        createAnimatedMetricCard(
            "Stock Saludable",
            String.valueOf(healthyCount),
            calculatePercentage(healthyCount, totalProducts),
            "var(--lumo-success-color)",
            VaadinIcon.CHECK_CIRCLE);

    Div criticalMetric =
        createAnimatedMetricCard(
            "Críticos",
            String.valueOf(criticalCount),
            calculatePercentage(criticalCount, totalProducts),
            "var(--lumo-error-color)",
            VaadinIcon.WARNING);

    Div lowMetric =
        createAnimatedMetricCard(
            "Stock Bajo",
            String.valueOf(lowCount),
            calculatePercentage(lowCount, totalProducts),
            "var(--lumo-warning-color)",
            VaadinIcon.ARROW_DOWN);

    Div outOfStockMetric =
        createAnimatedMetricCard(
            "Agotados",
            String.valueOf(outOfStockCount),
            calculatePercentage(outOfStockCount, totalProducts),
            "var(--lumo-error-color)",
            VaadinIcon.CLOSE_CIRCLE);

    metricsRow.add(healthMetric, criticalMetric, lowMetric, outOfStockMetric);
    return metricsRow;
  }

  private Div createAnimatedMetricCard(
      String title, String value, double percentage, String color, VaadinIcon icon) {
    Div card = new Div();
    card.addClassName("animated-metric-card");
    card.getStyle()
        .set("background", "white")
        .set("border-radius", "16px")
        .set("padding", "var(--lumo-space-l)")
        .set("box-shadow", "0 8px 32px rgba(0,0,0,0.1)")
        .set("border", "1px solid rgba(255,255,255,0.2)")
        .set("backdrop-filter", "blur(10px)")
        .set("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
        .set("min-width", "200px")
        .set("text-align", "center")
        .set("position", "relative")
        .set("overflow", "hidden");

    // Hover effect
    card.getElement()
        .addEventListener(
            "mouseenter",
            e -> {
              card.getStyle()
                  .set("transform", "translateY(-8px)")
                  .set("box-shadow", "0 16px 48px rgba(0,0,0,0.15)");
            });
    card.getElement()
        .addEventListener(
            "mouseleave",
            e -> {
              card.getStyle()
                  .set("transform", "translateY(0)")
                  .set("box-shadow", "0 8px 32px rgba(0,0,0,0.1)");
            });

    // Icon con gradiente
    Icon cardIcon = icon.create();
    cardIcon.setSize("32px");
    cardIcon
        .getStyle()
        .set("color", color)
        .set("margin-bottom", "var(--lumo-space-s)")
        .set("filter", "drop-shadow(0 2px 4px rgba(0,0,0,0.1))");

    // Valor principal
    Span valueSpan = new Span(value);
    valueSpan.addClassName(LumoUtility.FontSize.XXLARGE);
    valueSpan.addClassName(LumoUtility.FontWeight.BOLD);
    valueSpan
        .getStyle()
        .set("color", color)
        .set("text-shadow", "0 2px 4px rgba(0,0,0,0.1)")
        .set("display", "block")
        .set("margin-bottom", "var(--lumo-space-xs)");

    // Título
    Span titleSpan = new Span(title);
    titleSpan.addClassName(LumoUtility.FontSize.SMALL);
    titleSpan.addClassName(LumoUtility.FontWeight.MEDIUM);
    titleSpan
        .getStyle()
        .set("color", "var(--lumo-contrast-70pct)")
        .set("display", "block")
        .set("margin-bottom", "var(--lumo-space-s)");

    // Barra de progreso circular simulada con CSS
    ProgressBar progressBar = new ProgressBar();
    progressBar.setValue(percentage / 100.0);
    progressBar
        .getStyle()
        .set("width", "100%")
        .set("height", "4px")
        .set("--lumo-progress-color", color);

    // Porcentaje
    Span percentageSpan = new Span(String.format("%.1f%%", percentage));
    percentageSpan.addClassName(LumoUtility.FontSize.XSMALL);
    percentageSpan
        .getStyle()
        .set("color", color)
        .set("font-weight", "600")
        .set("margin-top", "var(--lumo-space-xs)");

    VerticalLayout cardContent = new VerticalLayout();
    cardContent.setPadding(false);
    cardContent.setSpacing(false);
    cardContent.setAlignItems(Alignment.CENTER);
    cardContent.add(cardIcon, valueSpan, titleSpan, progressBar, percentageSpan);

    card.add(cardContent);
    return card;
  }

  private Div createInteractiveAlertsPanel(List<StockAlertDto> stockAlerts) {
    Div alertsPanel = new Div();
    alertsPanel.addClassName("interactive-alerts-panel");
    alertsPanel
        .getStyle()
        .set("background", "white")
        .set("border-radius", "16px")
        .set("padding", "var(--lumo-space-l)")
        .set("margin-top", "var(--lumo-space-l)")
        .set("box-shadow", "0 4px 24px rgba(0,0,0,0.08)");

    H4 alertsTitle = new H4("Alertas Prioritarias");
    alertsTitle.addClassName(LumoUtility.Margin.NONE);
    alertsTitle
        .getStyle()
        .set("color", "var(--lumo-contrast-90pct)")
        .set("margin-bottom", "var(--lumo-space-m)")
        .set("font-weight", "700");

    // Grid moderno para mostrar alertas
    alertsGrid = new Grid<>(StockAlertDto.class, false);
    alertsGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
    alertsGrid.setHeight("300px");

    // Columna de estado con badges
    alertsGrid
        .addColumn(
            new ComponentRenderer<>(
                alert -> {
                  Span badge = new Span(getStatusLabel(alert.getAlertLevel()));
                  badge.getElement().getThemeList().add("badge");
                  badge
                      .getStyle()
                      .set("background", getAlertColor(alert.getAlertLevel()))
                      .set("color", "white")
                      .set("padding", "4px 12px")
                      .set("border-radius", "20px")
                      .set("font-size", "12px")
                      .set("font-weight", "600");
                  return badge;
                }))
        .setHeader("Estado")
        .setWidth("120px");

    // Columna de producto con iconos
    alertsGrid
        .addColumn(
            new ComponentRenderer<>(
                alert -> {
                  HorizontalLayout productLayout = new HorizontalLayout();
                  productLayout.setAlignItems(Alignment.CENTER);

                  Icon productIcon = VaadinIcon.PACKAGE.create();
                  productIcon.setSize("16px");
                  productIcon.getStyle().set("color", "var(--lumo-contrast-60pct)");

                  Span productName = new Span(alert.getProductName());
                  productName.getStyle().set("font-weight", "500");

                  productLayout.add(productIcon, productName);
                  return productLayout;
                }))
        .setHeader("Producto")
        .setFlexGrow(1);

    // Columna de stock con progress bar
    alertsGrid
        .addColumn(
            new ComponentRenderer<>(
                alert -> {
                  VerticalLayout stockLayout = new VerticalLayout();
                  stockLayout.setPadding(false);
                  stockLayout.setSpacing(false);

                  HorizontalLayout stockNumbers = new HorizontalLayout();
                  stockNumbers.setJustifyContentMode(JustifyContentMode.BETWEEN);
                  stockNumbers.setWidthFull();

                  Span currentStock = new Span("Actual: " + alert.getCurrentStock());
                  currentStock.addClassName(LumoUtility.FontSize.SMALL);

                  Span minStock = new Span("Mín: " + alert.getMinimumStock());
                  minStock.addClassName(LumoUtility.FontSize.SMALL);
                  minStock.getStyle().set("color", "var(--lumo-contrast-60pct)");

                  stockNumbers.add(currentStock, minStock);

                  ProgressBar stockProgress = new ProgressBar();
                  double progressValue =
                      Math.min(1.0, (double) alert.getCurrentStock() / alert.getMinimumStock());
                  stockProgress.setValue(progressValue);
                  stockProgress
                      .getStyle()
                      .set(
                          "--lumo-progress-color",
                          progressValue < 0.3
                              ? "var(--lumo-error-color)"
                              : progressValue < 0.7
                                  ? "var(--lumo-warning-color)"
                                  : "var(--lumo-success-color)");

                  stockLayout.add(stockNumbers, stockProgress);
                  return stockLayout;
                }))
        .setHeader("Stock")
        .setWidth("200px");

    // Columna de acciones
    alertsGrid
        .addColumn(
            new ComponentRenderer<>(
                alert -> {
                  Button actionBtn = new Button("Gestionar");
                  actionBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                  actionBtn.addClickListener(
                      e -> {
                        productService
                            .getProductById(alert.getProductId())
                            .ifPresentOrElse(
                                this::openProductForm,
                                () -> {
                                  Notification.show(
                                          "Producto no encontrado",
                                          3000,
                                          Notification.Position.TOP_END)
                                      .addThemeVariants(NotificationVariant.LUMO_ERROR);
                                });
                      });
                  return actionBtn;
                }))
        .setHeader("Acciones")
        .setWidth("120px");

    // Filtrar y mostrar alertas críticas
    List<StockAlertDto> criticalAlerts =
        stockAlerts.stream()
            .filter(
                alert ->
                    "CRITICAL".equals(alert.getAlertLevel())
                        || "OUT_OF_STOCK".equals(alert.getAlertLevel()))
            .limit(10)
            .collect(Collectors.toList());

    alertsGrid.setItems(criticalAlerts);

    alertsPanel.add(alertsTitle, alertsGrid);
    alertsPanel.setWidth("97%");
    return alertsPanel;
  }

  private void openProductForm(Product product) {
    productForm.openForEdit(product);
  }

  private void refreshStockAlertsGrid() {
    List<StockAlertDto> stockAlerts = dashboardService.getStockHealthAnalysis();
    List<StockAlertDto> criticalAlerts =
        stockAlerts.stream()
            .filter(
                alert ->
                    "CRITICAL".equals(alert.getAlertLevel())
                        || "OUT_OF_STOCK".equals(alert.getAlertLevel()))
            .limit(10)
            .collect(Collectors.toList());
    alertsGrid.setItems(criticalAlerts);
  }

  private Div createModernKPICard(String value, String title, Icon icon, String color) {
    Div card = new Div();
    card.addClassName("modern-kpi-card");
    card.getStyle()
        .set("background", "white")
        .set("border-radius", "12px")
        .set("padding", "var(--lumo-space-m)")
        .set("box-shadow", "0 4px 16px rgba(0,0,0,0.08)")
        .set("border", "1px solid rgba(255,255,255,0.2)")
        .set("transition", "all 0.3s ease")
        .set("min-width", "180px")
        .set("text-align", "center");

    // Hover effect
    card.getElement()
        .addEventListener(
            "mouseenter",
            e -> {
              card.getStyle().set("transform", "translateY(-4px)");
            });
    card.getElement()
        .addEventListener(
            "mouseleave",
            e -> {
              card.getStyle().set("transform", "translateY(0)");
            });

    icon.setSize("24px");
    icon.getStyle().set("color", color);

    Span valueSpan = new Span(value);
    valueSpan.addClassName(LumoUtility.FontSize.LARGE);
    valueSpan.addClassName(LumoUtility.FontWeight.BOLD);
    valueSpan.getStyle().set("color", color);

    Span titleSpan = new Span(title);
    titleSpan.addClassName(LumoUtility.FontSize.SMALL);
    titleSpan.getStyle().set("color", "var(--lumo-contrast-70pct)");

    VerticalLayout cardContent = new VerticalLayout(icon, valueSpan, titleSpan);
    cardContent.setSpacing(false);
    cardContent.setPadding(false);
    cardContent.setAlignItems(Alignment.CENTER);

    card.add(cardContent);
    return card;
  }

  private HorizontalLayout createAdvancedAnalyticsSection(List<StockAlertDto> stockAlerts) {
    HorizontalLayout analyticsSection = new HorizontalLayout();
    analyticsSection.setWidthFull();
    analyticsSection.setSpacing(true);

    // Gráfico de distribución modernizado
    Chart distributionChart = createModernDistributionChart(stockAlerts);
    distributionChart
        .getStyle()
        .set("background", "white")
        .set("border-radius", "16px")
        .set("box-shadow", "0 4px 24px rgba(0,0,0,0.08)");

    analyticsSection.add(distributionChart);
    return analyticsSection;
  }

  private Chart createModernDistributionChart(List<StockAlertDto> stockAlerts) {
    Chart chart = new Chart(ChartType.PIE);
    chart.setHeight("350px");

    Configuration config = chart.getConfiguration();
    config.setTitle("Distribución del Inventario");

    Map<String, Long> statusCounts =
        stockAlerts.stream()
            .collect(Collectors.groupingBy(StockAlertDto::getAlertLevel, Collectors.counting()));

    DataSeries series = new DataSeries();

    // Colores modernos y gradientes
    String[] modernColors = {
      "#10B981", // Verde éxito
      "#F59E0B", // Ámbar advertencia
      "#EF4444", // Rojo error
      "#8B5CF6" // Púrpura crítico
    };

    final int[] colorIndex = {0};
    statusCounts.forEach(
        (status, count) -> {
          DataSeriesItem item = new DataSeriesItem(getStatusLabel(status), count);
          item.setColor(new SolidColor(modernColors[colorIndex[0] % modernColors.length]));
          series.add(item);
          colorIndex[0]++;
        });

    long healthyCount = dashboardService.getHealthyStockCount();
    if (healthyCount > 0) {
      DataSeriesItem healthyItem = new DataSeriesItem("✅ Stock Saludable", healthyCount);
      healthyItem.setColor(new SolidColor("#059669"));
      series.add(healthyItem);
    }

    config.addSeries(series);

    // Tooltip moderno
    Tooltip tooltip = config.getTooltip();
    tooltip.setPointFormat("<b>{point.percentage:.1f}%</b><br/> Productos: {point.y}");

    return chart;
  }

  private void showStockAlerts() {
    List<StockAlertDto> criticalAlerts =
        dashboardService.getStockHealthAnalysis().stream()
            .filter(
                alert ->
                    "CRITICAL".equals(alert.getAlertLevel())
                        || "OUT_OF_STOCK".equals(alert.getAlertLevel()))
            .limit(3)
            .collect(Collectors.toList());

    criticalAlerts.forEach(
        alert -> {
          String message =
              String.format(
                  "%s: Stock crítico (%d unidades)",
                  alert.getProductName(), alert.getCurrentStock());

          Notification notification =
              Notification.show(message, 5000, Notification.Position.TOP_END);
          notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
  }

  private double calculatePercentage(long value, long total) {
    return total > 0 ? (double) value / total * 100 : 0;
  }

  private String getAlertColor(String status) {
    return switch (status) {
      case "CRITICAL" -> "#EF4444";
      case "LOW" -> "#F59E0B";
      case "OUT_OF_STOCK" -> "#DC2626";
      default -> "#10B981";
    };
  }

  private String getStatusLabel(String status) {
    return switch (status) {
      case "CRITICAL" -> "Crítico";
      case "LOW" -> "Bajo";
      case "OUT_OF_STOCK" -> "Agotado";
      default -> "Saludable";
    };
  }

  //----------------------------------------------------------
  private boolean isAdminOrManager() {
    return UserUtils.hasSystemRole(SystemRole.SYSTEM_ADMIN)
            || UserUtils.hasEmployeeRole(EmployeeRole.CLINIC_MANAGER);
  }

  // Opcional, si quieres mostrar el rol en el mensaje
  private String currentRoleLabel() {
    if (UserUtils.hasSystemRole(SystemRole.SYSTEM_ADMIN)) return "Administrador";
    if (UserUtils.hasEmployeeRole(EmployeeRole.CLINIC_MANAGER)) return "Gerente de Clínica";
    // agrega aquí otros roles si quieres mostrarlos
    return "Usuario";
  }

  // ====== Hero para no-admins ======
  private void dashboard(){
    this.productForm = new ProductForm(productService, supplierService, warehouseService);
    add(productForm);
    productForm.setOnSaveCallback(this::refreshStockAlertsGrid);

    initializeDashboard();
    showStockAlerts();
  }
  private void buildNonAdminHero(/* String roleLabel */) {
    removeAll();

    Div bg = new Div();
    bg.setSizeFull();
    bg.getStyle()
            .set("display", "grid")
            .set("place-items", "center")
            .set("background", "linear-gradient(135deg,#4FA7A4 0%,#2F6F6D 100%)");

    // “Badge” grande tipo la imagen (icono + texto)
    Div badge = new Div();
    badge.getStyle()
            .set("display", "inline-flex")
            .set("align-items", "center")
            .set("gap", "12px")
            .set("background", "#4FA7A4")
            .set("color", "white")
            .set("padding", "16px 24px")
            .set("border-radius", "8px")
            .set("box-shadow", "0 8px 24px rgba(0,0,0,0.20)");

    //Icon paw = VaadinIcon.STETHOSCOPE.create();
    //paw.setSize("28px");
    //paw.getStyle().set("color", "white");

    SvgIcon brandIcon = LineAwesomeIcon.PAW_SOLID.create();
    brandIcon.addClassNames(
            LumoUtility.IconSize.LARGE,
            LumoUtility.Margin.End.SMALL,
            LumoUtility.TextColor.PRIMARY_CONTRAST);

    Span brand = new Span("Zoolandia");
    brand.getStyle()
            .set("font-size", "22px")
            .set("font-weight", "700")
            .set("letter-spacing", "0.4px")
            .set("color", "white");

    badge.add(brandIcon, brand);

    Div text = new Div();
    text.getElement().setProperty("innerHTML",
            "<div style='color:white;text-align:center;text-shadow:0 1px 2px rgba(0,0,0,.3)'>"
                    + "  <div style='font-size:28px;font-weight:800;margin-top:16px'>Bienvenido a Zoolandia</div>"
                    + "  <div style='font-size:16px;opacity:.95;margin-top:6px'>"
                    + "    Tu cuenta no tiene acceso al Panel Ejecutivo.<br>"
                    + "    Si necesitas acceso, contacta al administrador."
                    + "  </div>"
                    // + "  <div style='font-size:14px;opacity:.85;margin-top:8px'>Rol: " + roleLabel + "</div>"
                    + "</div>");

    Button goHome = new Button("Volver al inicio", VaadinIcon.HOME.create(), e -> {
      getUI().ifPresent(ui -> ui.navigate("")); // o a la vista que prefieras
    });
    goHome.getStyle()
            .set("margin-top", "18px")
            .set("background", "white")
            .set("color", "#2F6F6D");

    Div container = new Div(badge, text);
    container.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("align-items", "center");

    bg.add(container);
    add(bg);
  }
}
