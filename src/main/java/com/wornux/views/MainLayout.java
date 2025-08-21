package com.wornux.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.SvgIcon;
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
import com.wornux.views.DashboardView;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIcon;

@Slf4j
@Layout
@PermitAll
public class MainLayout extends AppLayout {

    private H1 viewTitle;

    @Autowired
    private MenuService menuService;

    @Autowired
    private SecurityContextService securityContextService;
    
    private Scroller drawerScroller;
    private boolean menuInitialized = false;

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.TextColor.BODY
        );
        addHeaderContent();
        addDrawerContent();
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
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
        toggle.addClassNames(
            LumoUtility.Margin.End.MEDIUM,
            LumoUtility.TextColor.SECONDARY
        );

        viewTitle = new H1();
        viewTitle.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.FontWeight.SEMIBOLD,
            LumoUtility.Margin.NONE,
            LumoUtility.TextColor.HEADER,
            LumoUtility.Flex.GROW
        );

        MenuBar userMenu = createUserMenu();

        HorizontalLayout headerLayout = new HorizontalLayout(toggle, viewTitle, userMenu);
        headerLayout.addClassNames(
            LumoUtility.Width.FULL,
            LumoUtility.AlignItems.CENTER,
            LumoUtility.Padding.Horizontal.MEDIUM,
            LumoUtility.Padding.Vertical.SMALL,
            LumoUtility.Background.BASE,
            LumoUtility.BoxShadow.SMALL
        );
        headerLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerLayout.expand(viewTitle);

        addToNavbar(headerLayout);
    }

    private void addDrawerContent() {
        Div brandLayout = new Div();
        brandLayout.addClassNames(
            LumoUtility.Display.FLEX,
            LumoUtility.AlignItems.CENTER,
            LumoUtility.Padding.MEDIUM,
            LumoUtility.Background.PRIMARY,
            LumoUtility.TextColor.PRIMARY_CONTRAST
        );

        SvgIcon brandIcon = LineAwesomeIcon.PAW_SOLID.create();
        brandIcon.addClassNames(
            LumoUtility.IconSize.LARGE,
            LumoUtility.Margin.End.SMALL,
            LumoUtility.TextColor.PRIMARY_CONTRAST
        );

        H1 appName = new H1("Zoolandia");
        appName.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.FontWeight.BOLD,
            LumoUtility.Margin.NONE,
            LumoUtility.TextColor.PRIMARY_CONTRAST
        );

        brandLayout.add(brandIcon, appName);
        Header header = new Header(brandLayout);

        SideNav nav = createNavigation();
        nav.addClassNames(
            LumoUtility.Padding.Vertical.SMALL,
            LumoUtility.Background.BASE
        );

        drawerScroller = new Scroller(nav);
        drawerScroller.addClassNames(
            LumoUtility.Flex.GROW,
            LumoUtility.Background.BASE
        );

        Footer footer = createFooter();

        addToDrawer(header, drawerScroller, footer);
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        if (menuService == null || securityContextService == null) {
            log.debug("Menu services not yet available, creating default menu");
            nav.addItem(createNavItem("Inicio", DashboardView.class, LineAwesomeIcon.HOME_SOLID));
            return nav;
        }

        try {
            var menuItems = menuService.getMenuItemsForCurrentUser();

            for (var menuItem : menuItems) {
                nav.addItem(menuService.toSideNavItem(menuItem));
            }

            if (menuItems.isEmpty()) {
                log.warn("No menu items available for current user, adding default home item");
                nav.addItem(createNavItem("Inicio", DashboardView.class, LineAwesomeIcon.HOME_SOLID));
            }

            log.debug("Dynamic menu created with {} items", menuItems.size());
        } catch (Exception e) {
            log.error("Error creating dynamic menu, falling back to default", e);
            nav.addItem(createNavItem("Inicio", DashboardView.class, LineAwesomeIcon.HOME_SOLID));
        }

        return nav;
    }

    private SideNavItem createNavItem(String label, Class<? extends Component> navigationTarget, LineAwesomeIcon iconType) {
        SvgIcon icon = iconType.create();
        icon.addClassNames(
            LumoUtility.IconSize.MEDIUM,
            LumoUtility.TextColor.SECONDARY
        );
        
        SideNavItem item = new SideNavItem(label, navigationTarget, icon);
        item.addClassNames(
            LumoUtility.Padding.Vertical.XSMALL,
            LumoUtility.Padding.Horizontal.MEDIUM,
            LumoUtility.BorderRadius.MEDIUM,
            LumoUtility.Margin.Horizontal.SMALL
        );
        
        return item;
    }

    private MenuBar createUserMenu() {
        MenuBar menuBar = new MenuBar();
        menuBar.addClassNames(
            LumoUtility.Margin.Start.AUTO,
            LumoUtility.Background.TRANSPARENT
        );

        Avatar avatar = new Avatar();
        avatar.addClassNames(
            LumoUtility.Width.MEDIUM,
            LumoUtility.Height.MEDIUM
        );

        if (securityContextService != null) {
            String username = securityContextService.getCurrentUsername();
            avatar.setName(username);

            securityContextService.getCurrentSystemRole().ifPresent(
                role -> avatar.getElement().setAttribute("title", username + " - " + role.getDisplayName()));
        } else {
            avatar.setName("Usuario");
        }

        Span userName = new Span(avatar.getName());
        userName.addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY,
            LumoUtility.Margin.Start.SMALL
        );

        HorizontalLayout userInfo = new HorizontalLayout(avatar, userName);
        userInfo.addClassNames(
            LumoUtility.AlignItems.CENTER,
            LumoUtility.Padding.Horizontal.SMALL,
            LumoUtility.BorderRadius.MEDIUM
        );
        userInfo.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        MenuItem userMenuItem = menuBar.addItem(userInfo);
        SubMenu userSubMenu = userMenuItem.getSubMenu();
        
        userSubMenu.addItem(createMenuItemWithIcon(LineAwesomeIcon.USER_SOLID, "Perfil"), 
            e -> {});
        userSubMenu.addItem(createMenuItemWithIcon(LineAwesomeIcon.COG_SOLID, "Configuración"), 
            e -> {});
        userSubMenu.addSeparator();
        userSubMenu.addItem(createMenuItemWithIcon(LineAwesomeIcon.SIGN_OUT_ALT_SOLID, "Cerrar sesión"), 
            e -> getUI().ifPresent(ui -> ui.getPage().setLocation("/logout")));

        return menuBar;
    }

    private HorizontalLayout createMenuItemWithIcon(LineAwesomeIcon iconType, String text) {
        SvgIcon icon = iconType.create();
        icon.addClassNames(
            LumoUtility.IconSize.SMALL,
            LumoUtility.TextColor.SECONDARY
        );
        
        Span label = new Span(text);
        label.addClassNames(LumoUtility.Margin.Start.SMALL);
        
        HorizontalLayout layout = new HorizontalLayout(icon, label);
        layout.addClassNames(LumoUtility.AlignItems.CENTER);
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        
        return layout;
    }

    private Footer createFooter() {
        Footer footer = new Footer();
        footer.addClassNames(
            LumoUtility.Padding.MEDIUM,
            LumoUtility.Background.CONTRAST_5,
            LumoUtility.TextAlignment.CENTER,
            LumoUtility.BorderRadius.MEDIUM,
            LumoUtility.Margin.MEDIUM
        );

        Span footerText = new Span("© 2024 Zoolandia VetMgmt");
        footerText.addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.TERTIARY
        );

        footer.add(footerText);
        return footer;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        String className = getContent().getClass().getSimpleName();
        return className.replaceAll("View$", "").replaceAll("([a-z])([A-Z])", "$1 $2");
    }
}