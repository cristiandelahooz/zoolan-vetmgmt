package com.wornux.views.customers;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Client;
import com.wornux.dto.request.ClientCreateRequestDto;
import com.wornux.mapper.ClientMapper;
import com.wornux.services.interfaces.ClientService;
import com.wornux.views.clients.CompanyClientForm;
import com.wornux.views.clients.IndividualClientForm;
import lombok.Setter;

import java.util.function.Consumer;

/**
 * Dialog that allows users to choose between creating an individual client or a company client
 */
public class ClientCreationDialog extends Dialog {

    private final ClientService clientService;
    private final IndividualClientForm individualClientForm;
    private final CompanyClientForm companyClientForm;
    private final ClientMapper clientMapper;

    @Setter
    private Consumer<Client> onClientCreated;

    public ClientCreationDialog(ClientService clientService, ClientMapper clientMapper) {
        this.clientService = clientService;
        this.clientMapper = clientMapper;
        this.individualClientForm = new IndividualClientForm(clientService);
        this.companyClientForm = new CompanyClientForm(clientService);

        setupDialog();
        setupEventListeners();
    }

    private void setupDialog() {
        setHeaderTitle("Crear Nuevo Cliente");
        setModal(true);
        setWidth("500px");
        setMaxWidth("90vw");
        setHeight("auto");
        setMaxHeight("90vh");
        setCloseOnOutsideClick(false);
        setCloseOnEsc(true);

        // Create content
        H3 title = new H3("¿Qué tipo de cliente deseas crear?");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.TextAlignment.CENTER);

        Paragraph description = new Paragraph(
                "Selecciona el tipo de cliente que deseas agregar. Puedes crear un cliente individual "
                        + "o una empresa según tus necesidades.");
        description.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Bottom.LARGE);

        // Individual client button
        Button individualButton = createClientTypeButton("Cliente Individual", "Persona física con cédula o pasaporte",
                VaadinIcon.USER, this::openIndividualForm);

        // Company client button  
        Button companyButton = createClientTypeButton("Empresa", "Empresa o negocio con RNC", VaadinIcon.BUILDING,
                this::openCompanyForm);

        VerticalLayout buttonLayout = new VerticalLayout(individualButton, companyButton);
        buttonLayout.addClassNames(LumoUtility.JustifyContent.CENTER, LumoUtility.Gap.SMALL, LumoUtility.Width.FULL);
        buttonLayout.setSpacing(false);

        VerticalLayout content = new VerticalLayout(title, description, buttonLayout);
        content.addClassNames(LumoUtility.Padding.LARGE, LumoUtility.AlignItems.CENTER);

        add(content);

        // Cancel button in footer
        Button cancelButton = new Button("Cancelar", e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getFooter().add(cancelButton);
    }

    private Button createClientTypeButton(String title, String description, VaadinIcon icon, Runnable action) {
        Div iconDiv = new Div(icon.create());
        iconDiv.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER, LumoUtility.Background.PRIMARY_10,
                LumoUtility.BorderRadius.LARGE, LumoUtility.Width.LARGE, LumoUtility.Height.LARGE,
                LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.TextColor.PRIMARY);
        iconDiv.getStyle().set("font-size", "2rem");

        H3 buttonTitle = new H3(title);
        buttonTitle.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontSize.MEDIUM,
                LumoUtility.FontWeight.SEMIBOLD);

        Paragraph buttonDescription = new Paragraph(description);
        buttonDescription.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY);

        VerticalLayout buttonContent = new VerticalLayout(iconDiv, buttonTitle, buttonDescription);
        buttonContent.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.Padding.LARGE, LumoUtility.Gap.SMALL);
        buttonContent.setSpacing(false);

        Button button = new Button(buttonContent);
        button.addThemeVariants(ButtonVariant.LUMO_LARGE);
        button.addClassNames(LumoUtility.Width.FULL, LumoUtility.Height.AUTO);
        button.getStyle().set("min-height", "120px").set("border", "2px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-l)").set("cursor", "pointer");

        // Hover effects
        button.getElement().addEventListener("mouseenter",
                e -> button.getStyle().set("border-color", "var(--lumo-primary-color)"));
        button.getElement().addEventListener("mouseleave",
                e -> button.getStyle().set("border-color", "var(--lumo-contrast-20pct)"));

        button.addClickListener(e -> {
            close();
            action.run();
        });

        return button;
    }

    private void setupEventListeners() {
        // Setup listeners for both forms
        individualClientForm.addClientSavedListener(this::handleClientCreated);
        companyClientForm.addClientSavedListener(this::handleClientCreated);

        individualClientForm.addClientCancelledListener(this::handleClientCancelled);
        companyClientForm.addClientCancelledListener(this::handleClientCancelled);
    }

    private void openIndividualForm() {
        individualClientForm.openForNew();
    }

    private void openCompanyForm() {
        companyClientForm.openForNew();
    }

    private void handleClientCreated(ClientCreateRequestDto dto) {
        // Convert DTO to Client entity using mapper
        if (clientMapper != null && onClientCreated != null) {
            Client client = clientMapper.toEntity(dto);
            onClientCreated.accept(client);
        }
    }

    private void handleClientCancelled() {
        // Optionally reopen the selection dialog or do nothing
        // For now, just close everything
    }

    public void openDialog() {
        open();
    }

    /**
     * Sets the callback to be executed when a client is successfully created
     *
     * @param callback
     *            Consumer that receives the created client
     */
    public void setOnClientCreated(Consumer<Client> callback) {
        this.onClientCreated = callback;
    }
}
