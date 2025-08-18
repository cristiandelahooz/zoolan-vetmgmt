package com.wornux.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import com.wornux.views.appointments.AppointmentsCalendarView;
import com.wornux.views.clients.CompanyClientView;
import com.wornux.views.clients.IndividualClientView;
import com.wornux.views.consultations.ConsultationsView;
import com.wornux.views.employees.EmployeeView;
import com.wornux.views.inventory.InventoryView;
import com.wornux.views.medicalhistory.MedicalHistoryView;
import com.wornux.views.pets.PetMergeView;
import com.wornux.views.pets.PetView;
import com.wornux.views.suppliers.SupplierView;
import com.wornux.views.transactions.InvoiceView;
import com.wornux.views.waitingroom.WaitingRoomView;
import com.wornux.views.warehouses.WarehouseView;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@PermitAll
public class MainLayout extends AppLayout {

  private H2 viewTitle;

  public MainLayout() {
    setPrimarySection(Section.DRAWER);
    addHeaderContent();
    addDrawerContent();
  }

  private void addHeaderContent() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.setAriaLabel("Menu toggle");

    viewTitle = new H2();
    viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

    // User menu
    MenuBar userMenu = createUserMenu();

    HorizontalLayout header = new HorizontalLayout(toggle, viewTitle);
    header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    header.expand(viewTitle);
    header.add(userMenu);
    header.setWidthFull();
    header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

    addToNavbar(true, header);
  }

  private void addDrawerContent() {
    H1 appName = new H1("Zoolandia");
    appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
    Header header = new Header(appName);

    Scroller scroller = new Scroller(createNavigation());

    addToDrawer(header, scroller);
  }

  private SideNav createNavigation() {
    SideNav nav = new SideNav();

    // Dashboard/Home
    nav.addItem(new SideNavItem("Inicio", DashboardView.class, VaadinIcon.HOME_O.create()));

    // Appointments (Calendar)
    nav.addItem(
        new SideNavItem("Citas", AppointmentsCalendarView.class, VaadinIcon.CALENDAR.create()));

    // Inventory Section
    SideNavItem inventorySection = new SideNavItem("Inventario");
    inventorySection.setPrefixComponent(VaadinIcon.ARCHIVES.create());
    inventorySection.addItem(
        new SideNavItem("Productos", InventoryView.class, VaadinIcon.CART.create()));
    inventorySection.addItem(
        new SideNavItem("Almacenes", WarehouseView.class, VaadinIcon.TRUCK.create()));
    nav.addItem(inventorySection);

    // Consultations
    nav.addItem(
        new SideNavItem("Consultas", ConsultationsView.class, VaadinIcon.STETHOSCOPE.create()));

    // Transactions Section
    SideNavItem transactionsSection = new SideNavItem("Transacciones");
    transactionsSection.setPrefixComponent(VaadinIcon.CREDIT_CARD.create());
    transactionsSection.addItem(
        new SideNavItem("Facturas", InvoiceView.class, VaadinIcon.FILE_TEXT.create()));
    nav.addItem(transactionsSection);

    // Waiting Room
    nav.addItem(
        new SideNavItem("Sala de Espera", WaitingRoomView.class, VaadinIcon.OFFICE.create()));

    // Clients Section
    SideNavItem clientsSection = new SideNavItem("Clientes");
    clientsSection.setPrefixComponent(VaadinIcon.USER.create());
    clientsSection.addItem(
        new SideNavItem("Individuales", IndividualClientView.class, VaadinIcon.USER.create()));
    clientsSection.addItem(
        new SideNavItem("Empresariales", CompanyClientView.class, VaadinIcon.BUILDING.create()));
    nav.addItem(clientsSection);

    // Pets Section
    SideNavItem petsSection = new SideNavItem("Mascotas", PetView.class);
    petsSection.setPrefixComponent(createPetsIcon());
    petsSection.addItem(
        new SideNavItem("Fusionar", PetMergeView.class, VaadinIcon.CONNECT.create()));
    nav.addItem(petsSection);

    // Employees
    nav.addItem(new SideNavItem("Empleados", EmployeeView.class, VaadinIcon.DOCTOR.create()));

    // Suppliers
    nav.addItem(new SideNavItem("Suplidores", SupplierView.class, VaadinIcon.TRUCK.create()));

    // Medical History
    nav.addItem(new SideNavItem("Historial Médico", MedicalHistoryView.class,
        VaadinIcon.CLIPBOARD_TEXT.create()));

    return nav;
  }

  private Icon createPetsIcon() {
    // Using a simple icon for pets since LineAwesome causes issues
    return VaadinIcon.HEART.create();  // Using heart icon for pets
  }

  private MenuBar createUserMenu() {
    MenuBar menuBar = new MenuBar();
    menuBar.setThemeName("tertiary-inline contrast");

    Avatar avatar = new Avatar();
    avatar.setName("Usuario");
    avatar.setThemeName("xsmall");

    MenuItem userMenuItem = menuBar.addItem(avatar);
    SubMenu userSubMenu = userMenuItem.getSubMenu();

    userSubMenu.addItem("Perfil", e -> {
      // Navigate to profile
    });
    userSubMenu.addItem("Configuración", e -> {
      // Navigate to settings
    });
    userSubMenu.addSeparator();
    userSubMenu.addItem("Cerrar sesión", e -> {
      getUI().ifPresent(ui -> ui.getPage().setLocation("/logout"));
    });

    return menuBar;
  }

  @Override
  protected void afterNavigation() {
    super.afterNavigation();
    viewTitle.setText(getCurrentPageTitle());
  }

  private String getCurrentPageTitle() {
    return getContent().getClass().getSimpleName()
        .replaceAll("View$", "")
        .replaceAll("([a-z])([A-Z])", "$1 $2");
  }
}
