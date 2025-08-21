package com.wornux.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.security.service.MenuService;
import com.wornux.security.service.SecurityContextService;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The main view is a top-level placeholder for other views.
 */
@Slf4j
@Layout
@PermitAll
public class MainLayout extends AppLayout {

    private H2 viewTitle;

    @Autowired
    private MenuService menuService;

    @Autowired
    private SecurityContextService securityContextService;
    
    private Scroller drawerScroller;
    private boolean menuInitialized = false;

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addHeaderContent();
        addDrawerContent();
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Rebuild menu when attached and services are available
        if (!menuInitialized && menuService != null && securityContextService != null) {
            rebuildNavigationMenu();
            menuInitialized = true;
        }
    }
    
    private void rebuildNavigationMenu() {
        if (drawerScroller != null) {
            SideNav newNav = createNavigation();
            drawerScroller.setContent(newNav);
            log.info("Navigation menu rebuilt for user: {}", securityContextService.getCurrentUsername());
        }
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

        drawerScroller = new Scroller(createNavigation());

        addToDrawer(header, drawerScroller);
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        // Check if services are available (they might not be during initial construction)
        if (menuService == null || securityContextService == null) {
            log.debug("Menu services not yet available, creating default menu");
            // Return empty nav or minimal nav
            nav.addItem(new SideNavItem("Inicio", DashboardView.class, VaadinIcon.HOME_O.create()));
            return nav;
        }

        try {
            // Get menu items for current user
            var menuItems = menuService.getMenuItemsForCurrentUser();

            // Convert menu items to SideNav items
            for (var menuItem : menuItems) {
                nav.addItem(menuService.toSideNavItem(menuItem));
            }

            // If no items were added, add at least the home item
            if (menuItems.isEmpty()) {
                log.warn("No menu items available for current user, adding default home item");
                nav.addItem(new SideNavItem("Inicio", DashboardView.class, VaadinIcon.HOME_O.create()));
            }

            log.debug("Dynamic menu created with {} items", menuItems.size());
        } catch (Exception e) {
            log.error("Error creating dynamic menu, falling back to default", e);
            // Fallback to a minimal menu
            nav.addItem(new SideNavItem("Inicio", DashboardView.class, VaadinIcon.HOME_O.create()));
        }

        return nav;
    }

    private MenuBar createUserMenu() {
        MenuBar menuBar = new MenuBar();
        menuBar.setThemeName("tertiary-inline contrast");

        Avatar avatar = new Avatar();

        // Set user name from security context if available
        if (securityContextService != null) {
            String username = securityContextService.getCurrentUsername();
            avatar.setName(username);

            // Add role information to title attribute for hover text
            securityContextService.getCurrentSystemRole().ifPresent(
                    role -> avatar.getElement().setAttribute("title", username + " - " + role.getDisplayName()));
        } else {
            avatar.setName("Usuario");
        }

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
        return getContent().getClass().getSimpleName().replaceAll("View$", "").replaceAll("([a-z])([A-Z])", "$1 $2");
    }
}
