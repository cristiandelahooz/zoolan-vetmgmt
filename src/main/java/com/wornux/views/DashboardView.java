package com.wornux.views;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardSection;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;

@RolesAllowed({"ROLE_SYSTEM_ADMIN", "ROLE_MANAGER", "ROLE_USER"})
@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard")
public class DashboardView extends VerticalLayout {

  public DashboardView() {
    Dashboard dashboard = new Dashboard();
    dashboard.setMinimumColumnWidth("150px");
    dashboard.setMaximumColumnCount(3);
    DashboardSection dashboardsection = new DashboardSection("Monthly Funnel Stats");
    DashboardWidget dashboardwidget = new DashboardWidget("Visitors");
    Div div = new Div();
    div.setClassName("dashboard-widget-content");
    Chart chart2 = new Chart(ChartType.PIE);
    chart2.setMinHeight("400px");
    chart2.getConfiguration().setTitle("");
    DataSeries dataseries2 = new DataSeries("The data");
    DataSeriesItem dataseriesitem5 = new DataSeriesItem();
    dataseriesitem5.setName("Product 1");
    dataseriesitem5.setY(1);
    DataSeriesItem dataseriesitem6 = new DataSeriesItem();
    dataseriesitem6.setName("Product 2");
    dataseriesitem6.setY(2);
    DataSeriesItem dataseriesitem7 = new DataSeriesItem();
    dataseriesitem7.setName("Product 3");
    dataseriesitem7.setY(4);
    dataseries2.setData(List.of(dataseriesitem5, dataseriesitem6, dataseriesitem7));
    chart2.getConfiguration().addSeries(dataseries2);
    div.add(chart2);
    DataSeries dataseries = new DataSeries("Sales");
    DataSeriesItem dataseriesitem = new DataSeriesItem();
    dataseriesitem.setName("Product A");
    dataseriesitem.setY(42112);
    DataSeriesItem dataseriesitem2 = new DataSeriesItem();
    dataseriesitem2.setName("Product B");
    dataseriesitem2.setY(58698);
    DataSeriesItem dataseriesitem3 = new DataSeriesItem();
    dataseriesitem3.setName("Product C");
    dataseriesitem3.setY(12276);
    DataSeriesItem dataseriesitem4 = new DataSeriesItem();
    dataseriesitem4.setName("Product D");
    dataseriesitem4.setY(33202);
    dataseries.setData(List.of(dataseriesitem, dataseriesitem2, dataseriesitem3, dataseriesitem4));
    dashboardwidget.setContent(div);
    dashboardsection.add(dashboardwidget);
    DashboardWidget dashboardwidget2 = new DashboardWidget("Downloads");
    Div div2 = new Div();
    div2.setClassName("dashboard-widget-content");
    Chart chart = new Chart(ChartType.COLUMN);
    chart.setMinHeight("400px");
    chart.getConfiguration().setTitle("Sales 2023");
    chart
        .getConfiguration()
        .getxAxis()
        .setCategories(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    chart.getConfiguration().getyAxis().setTitle("Euro (€)");
    ListSeries listseries = new ListSeries("Product A");
    listseries.setData(
        42112, 58698, 12276, 33202, 74518, 45498, 42477, 17896, 44297, 22456, 38547, 12621);
    chart.getConfiguration().addSeries(listseries);
    ListSeries listseries2 = new ListSeries("Product B");
    listseries2.setData(
        70972, 48589, 94434, 58270, 77282, 7108, 54085, 44401, 28868, 79643, 14383, 76036);
    chart.getConfiguration().addSeries(listseries2);
    div2.add(chart);
    dashboardwidget2.setContent(div2);
    dashboardsection.add(dashboardwidget2);
    DashboardWidget dashboardwidget3 = new DashboardWidget("Conversions");
    Div div3 = new Div();
    div3.setClassName("dashboard-widget-content");
    Div div8 = new Div();
    Div div9 = new Div();
    Div div10 = new Div();
    div10.getStyle().set("width", "126px").set("height", "96px");
    div10.addClassNames(LumoUtility.BorderRadius.SMALL);
    div9.add(div10);
    Div div11 = new Div();
    div9.add(div11);
    div8.add(div9);
    div3.add(div8);
    dashboardwidget3.setContent(div3);
    dashboardsection.add(dashboardwidget3);
    dashboard.addSection(dashboardsection);
    DashboardSection dashboardsection2 = new DashboardSection("Visitor Details");
    DashboardWidget dashboardwidget4 = new DashboardWidget("Visitors by country");
    dashboardwidget4.setRowspan(2);
    Div div4 = new Div();
    div4.setClassName("dashboard-widget-content");
    dashboardwidget4.setContent(div4);
    dashboardsection2.add(dashboardwidget4);
    DashboardWidget dashboardwidget5 = new DashboardWidget("Browsers");
    Div div5 = new Div();
    div5.setClassName("dashboard-widget-content");
    dashboardwidget5.setContent(div5);
    dashboardsection2.add(dashboardwidget5);
    DashboardWidget dashboardwidget6 = new DashboardWidget("A kittykat!");
    Div div6 = new Div();
    div6.setClassName("dashboard-widget-content");
    dashboardwidget6.setContent(div6);
    dashboardsection2.add(dashboardwidget6);
    DashboardWidget dashboardwidget7 = new DashboardWidget("Visitors by browser");
    dashboardwidget7.setColspan(2);
    Div div7 = new Div();
    div7.setClassName("dashboard-widget-content");
    dashboardwidget7.setContent(div7);
    dashboardsection2.add(dashboardwidget7);
    dashboard.addSection(dashboardsection2);
    add(dashboard);
  }
}
