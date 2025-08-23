package com.wornux.utils;

import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.wornux.views.DashboardView;
import com.wornux.views.appointments.AppointmentsCalendarView;
import com.wornux.views.clients.CompanyClientView;
import com.wornux.views.clients.IndividualClientView;
import com.wornux.views.consultations.ConsultationsView;
import com.wornux.views.employees.EmployeeView;
import com.wornux.views.inventory.InventoryView;
import com.wornux.views.pets.PetMergeView;
import com.wornux.views.pets.PetView;
import com.wornux.views.suppliers.SupplierView;
import com.wornux.views.transactions.InvoiceView;
import com.wornux.views.waitingroom.WaitingRoomView;
import com.wornux.views.warehouses.WarehouseView;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.lineawesome.LineAwesomeIcon;

@Slf4j
public class MenuUtil {
  private MenuUtil() {
    // It's not required
  }

  public static SideNav getMenuItemsForCurrentUser(AccessAnnotationChecker accessChecker) {
    SideNav nav = new SideNav();

    if (accessChecker.hasAccess(DashboardView.class))
      nav.addItem(
          new SideNavItem("Inicio", DashboardView.class, LineAwesomeIcon.HOME_SOLID.create()));

    if (accessChecker.hasAccess(AppointmentsCalendarView.class))
      nav.addItem(
          new SideNavItem(
              "Citas",
              AppointmentsCalendarView.class,
              LineAwesomeIcon.CALENDAR_ALT_SOLID.create()));

    if (accessChecker.hasAccess(InventoryView.class)
        || accessChecker.hasAccess(WarehouseView.class)) {
      SideNavItem inventory = new SideNavItem("Inventario");
      inventory.setPrefixComponent(LineAwesomeIcon.BOXES_SOLID.create());

      if (accessChecker.hasAccess(InventoryView.class))
        inventory.addItem(
            new SideNavItem("Productos", InventoryView.class, LineAwesomeIcon.BOX_SOLID.create()));
      if (accessChecker.hasAccess(WarehouseView.class))
        inventory.addItem(
            new SideNavItem(
                "Almacenes", WarehouseView.class, LineAwesomeIcon.WAREHOUSE_SOLID.create()));

      nav.addItem(inventory);
    }

    if (accessChecker.hasAccess(ConsultationsView.class))
      nav.addItem(
          new SideNavItem(
              "Consultas", ConsultationsView.class, LineAwesomeIcon.USER_MD_SOLID.create()));

    if (accessChecker.hasAccess(InvoiceView.class)) {
      SideNavItem transactions = new SideNavItem("Transacciones");
      transactions.setPrefixComponent(LineAwesomeIcon.CREDIT_CARD_SOLID.create());
      transactions.addItem(
          new SideNavItem(
              "Facturas", InvoiceView.class, LineAwesomeIcon.FILE_INVOICE_DOLLAR_SOLID.create()));
      nav.addItem(transactions);
    }

    if (accessChecker.hasAccess(WaitingRoomView.class))
      nav.addItem(
          new SideNavItem(
              "Sala de Espera", WaitingRoomView.class, LineAwesomeIcon.COUCH_SOLID.create()));

    if (accessChecker.hasAccess(IndividualClientView.class)
        || accessChecker.hasAccess(CompanyClientView.class)) {
      SideNavItem clients = new SideNavItem("Clientes");
      clients.setPrefixComponent(LineAwesomeIcon.USERS_SOLID.create());
      if (accessChecker.hasAccess(IndividualClientView.class))
        clients.addItem(
            new SideNavItem(
                "Individuales", IndividualClientView.class, LineAwesomeIcon.USER_SOLID.create()));
      if (accessChecker.hasAccess(CompanyClientView.class))
        clients.addItem(
            new SideNavItem(
                "Empresariales", CompanyClientView.class, LineAwesomeIcon.BUILDING_SOLID.create()));
      nav.addItem(clients);
    }

    if (accessChecker.hasAccess(PetView.class)) {
      SideNavItem pets =
          new SideNavItem("Mascotas", PetView.class, LineAwesomeIcon.PAW_SOLID.create());
      if (accessChecker.hasAccess(PetMergeView.class))
        pets.addItem(
            new SideNavItem(
                "Fusionar", PetMergeView.class, LineAwesomeIcon.CODE_BRANCH_SOLID.create()));
      nav.addItem(pets);
    }

    if (accessChecker.hasAccess(EmployeeView.class))
      nav.addItem(
          new SideNavItem(
              "Empleados", EmployeeView.class, LineAwesomeIcon.USER_TIE_SOLID.create()));

    if (accessChecker.hasAccess(SupplierView.class))
      nav.addItem(
          new SideNavItem("Suplidores", SupplierView.class, LineAwesomeIcon.TRUCK_SOLID.create()));

    return nav;
  }
}
