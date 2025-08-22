package com.wornux.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.security.UserUtils;
import com.wornux.utils.MenuUtil;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.lineawesome.LineAwesomeIcon;

@Slf4j
@Layout
@PermitAll
public class MainLayout extends AppLayout {

    private final AccessAnnotationChecker accessChecker;
    private H1 viewTitle;

    public MainLayout(AccessAnnotationChecker accessChecker) {
        this.accessChecker = accessChecker;
        setPrimarySection(Section.DRAWER);
        addClassNames(LumoUtility.Background.BASE, LumoUtility.TextColor.BODY);
        addHeaderContent();
        addDrawerContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");
        toggle.addClassNames(LumoUtility.Margin.End.MEDIUM, LumoUtility.TextColor.SECONDARY);

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.SEMIBOLD, LumoUtility.Margin.NONE,
                LumoUtility.TextColor.HEADER, LumoUtility.Flex.GROW);

        MenuBar userMenu = createUserMenu();

        HorizontalLayout headerLayout = new HorizontalLayout(toggle, viewTitle, userMenu);
        headerLayout.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER,
                LumoUtility.Padding.Horizontal.MEDIUM, LumoUtility.Padding.Vertical.SMALL, LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.SMALL);
        headerLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerLayout.expand(viewTitle);

        addToNavbar(headerLayout);
    }

    private void addDrawerContent() {
        Div brandLayout = new Div();
        brandLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.Padding.MEDIUM,
                LumoUtility.Background.PRIMARY, LumoUtility.TextColor.PRIMARY_CONTRAST);

        SvgIcon brandIcon = LineAwesomeIcon.PAW_SOLID.create();
        brandIcon.addClassNames(LumoUtility.IconSize.LARGE, LumoUtility.Margin.End.SMALL,
                LumoUtility.TextColor.PRIMARY_CONTRAST);

        H1 appName = new H1("Zoolandia");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD, LumoUtility.Margin.NONE,
                LumoUtility.TextColor.PRIMARY_CONTRAST);

        brandLayout.add(brandIcon, appName);
        Header header = new Header(brandLayout);

        SideNav nav = createNavigation();
        nav.addClassNames(LumoUtility.Padding.Vertical.SMALL, LumoUtility.Background.BASE);

        Scroller drawerScroller = new Scroller(nav);
        drawerScroller.addClassNames(LumoUtility.Flex.GROW, LumoUtility.Background.BASE);

        Footer footer = createFooter();

        addToDrawer(header, drawerScroller, footer);
    }

    private SideNav createNavigation() {
        return MenuUtil.getMenuItemsForCurrentUser(accessChecker);
    }

    private MenuBar createUserMenu() {
        MenuBar menuBar = new MenuBar();
        menuBar.addClassNames(LumoUtility.Margin.Start.AUTO, LumoUtility.Background.TRANSPARENT);

        Avatar avatar = new Avatar();
        avatar.addClassNames(LumoUtility.Width.MEDIUM, LumoUtility.Height.MEDIUM);

        if (UserUtils.getUser().isPresent()) {
            String username = UserUtils.getCurrentUsername();
            avatar.setName(username);

            UserUtils.getCurrentSystemRole().ifPresent(role -> avatar.getElement().setAttribute("title",
                    username + " - " + role.getDisplayName()));
        } else {
            avatar.setName("Usuario");
        }

        Span userName = new Span(avatar.getName());
        userName.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
                LumoUtility.Margin.Start.SMALL);

        HorizontalLayout userInfo = new HorizontalLayout(avatar, userName);
        userInfo.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.Padding.Horizontal.SMALL,
                LumoUtility.BorderRadius.MEDIUM);
        userInfo.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        MenuItem userMenuItem = menuBar.addItem(userInfo);
        SubMenu userSubMenu = userMenuItem.getSubMenu();

        userSubMenu.addSeparator();
        userSubMenu.addItem(createMenuItemWithIcon(), e -> getUI().ifPresent(ui -> ui.getPage().setLocation(
                "/logout")));

        return menuBar;
    }

    private HorizontalLayout createMenuItemWithIcon() {
        SvgIcon icon = LineAwesomeIcon.SIGN_OUT_ALT_SOLID.create();
        icon.addClassNames(LumoUtility.IconSize.SMALL, LumoUtility.TextColor.SECONDARY);

        Span label = new Span("Cerrar sesión");
        label.addClassNames(LumoUtility.Margin.Start.SMALL);

        HorizontalLayout layout = new HorizontalLayout(icon, label);
        layout.addClassNames(LumoUtility.AlignItems.CENTER);
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        return layout;
    }

    private Footer createFooter() {
        Footer footer = new Footer();
        footer.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.Background.CONTRAST_5,
                LumoUtility.TextAlignment.CENTER, LumoUtility.BorderRadius.MEDIUM, LumoUtility.Margin.MEDIUM);

        Span footerText = new Span("© 2024 Zoolandia VetMgmt");
        footerText.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.TERTIARY);

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
