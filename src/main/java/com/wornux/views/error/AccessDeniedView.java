package com.wornux.views.error;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * View displayed when a user tries to access a resource they don't have permission for.
 * Provides a user-friendly message and navigation options.
 */
@Slf4j
@Route("access-denied")
@PageTitle("Acceso Denegado")
@AnonymousAllowed
public class AccessDeniedView extends VerticalLayout implements HasUrlParameter<String>, AfterNavigationObserver {
    
    private String message = "No tiene permisos para acceder a esta sección.";
    private String attemptedRoute = "";
    
    public AccessDeniedView() {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        addClassName(LumoUtility.Background.CONTRAST_5);
        
        createContent();
    }
    
    private void createContent() {
        // Clear any existing content
        removeAll();
        
        // Container for the content
        Div container = new Div();
        container.addClassName(LumoUtility.Background.BASE);
        container.addClassName(LumoUtility.BorderRadius.LARGE);
        container.addClassName(LumoUtility.Padding.XLARGE);
        container.addClassName(LumoUtility.BoxShadow.MEDIUM);
        container.setMaxWidth("600px");
        
        // Icon
        Icon lockIcon = VaadinIcon.LOCK.create();
        lockIcon.setSize("64px");
        lockIcon.setColor("var(--lumo-error-color)");
        
        // Title
        H2 title = new H2("Acceso Denegado");
        title.addClassName(LumoUtility.Margin.Top.NONE);
        title.addClassName(LumoUtility.Margin.Bottom.SMALL);
        title.addClassName(LumoUtility.TextColor.ERROR);
        
        // Message
        Paragraph messageParagraph = new Paragraph(message);
        messageParagraph.addClassName(LumoUtility.TextColor.SECONDARY);
        messageParagraph.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
        
        // Additional info if we know what route was attempted
        if (!attemptedRoute.isEmpty()) {
            Paragraph attemptInfo = new Paragraph("Intentó acceder a: " + attemptedRoute);
            attemptInfo.addClassName(LumoUtility.FontSize.SMALL);
            attemptInfo.addClassName(LumoUtility.TextColor.TERTIARY);
            container.add(attemptInfo);
        }
        
        // Buttons
        Button homeButton = new Button("Ir al Inicio", VaadinIcon.HOME.create());
        homeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        homeButton.addClickListener(e -> {
            log.info("User redirected to home from access denied page");
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        homeButton.addClickShortcut(Key.ENTER);
        
        Button backButton = new Button("Volver", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> {
            log.info("User going back from access denied page");
            getUI().ifPresent(ui -> ui.getPage().getHistory().back());
        });
        backButton.addClickShortcut(Key.ESCAPE);
        
        // Button layout
        Div buttonLayout = new Div(homeButton, backButton);
        buttonLayout.addClassName(LumoUtility.Display.FLEX);
        buttonLayout.addClassName(LumoUtility.Gap.MEDIUM);
        buttonLayout.addClassName(LumoUtility.Margin.Top.LARGE);
        
        // Content layout
        VerticalLayout content = new VerticalLayout(lockIcon, title, messageParagraph, buttonLayout);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setPadding(false);
        content.setSpacing(true);
        
        container.add(content);
        add(container);
    }
    
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        // The parameter can contain the custom message
        if (parameter != null && !parameter.isEmpty()) {
            try {
                // Decode the message if it was URL encoded
                this.message = java.net.URLDecoder.decode(parameter, "UTF-8");
            } catch (Exception e) {
                log.error("Error decoding access denied message", e);
                this.message = parameter;
            }
        }
        
        // Try to get the attempted route from query parameters
        event.getLocation().getQueryParameters().getParameters().forEach((key, values) -> {
            if ("route".equals(key) && !values.isEmpty()) {
                this.attemptedRoute = values.get(0);
            }
        });
    }
    
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // Update content after navigation to ensure parameters are set
        createContent();
        
        // Log the access denied event
        log.warn("Access denied page shown. Message: '{}', Attempted route: '{}'", 
                message, attemptedRoute);
    }
}