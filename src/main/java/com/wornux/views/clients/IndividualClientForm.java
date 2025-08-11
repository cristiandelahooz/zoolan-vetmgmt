package com.wornux.views.clients;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.enums.*;
import com.wornux.dto.request.ClientCreateRequestDto;
import com.wornux.services.interfaces.ClientService;
import com.wornux.utils.NotificationUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class IndividualClientForm extends Dialog {

    // Personal Information
    private final TextField firstName = new TextField("Nombre");
    private final TextField lastName = new TextField("Apellido");
    private final EmailField email = new EmailField("Correo Electrónico");
    private final TextField phoneNumber = new TextField("Teléfono");
    private final DatePicker birthDate = new DatePicker("Fecha de Nacimiento");
    private final ComboBox<Gender> gender = new ComboBox<>("Género");
    private final TextField nationality = new TextField("Nacionalidad");

    // Identification (Individual)
    private final TextField cedula = new TextField("Cédula");
    private final TextField passport = new TextField("Pasaporte");

    // Contact Information
    private final ComboBox<PreferredContactMethod> preferredContactMethod = new ComboBox<>(
            "Método de Contacto Preferido");
    private final TextField emergencyContactName = new TextField("Nombre del Contacto de Emergencia");
    private final TextField emergencyContactNumber = new TextField("Teléfono de Emergencia");

    // Address Information
    private final TextField province = new TextField("Provincia");
    private final TextField municipality = new TextField("Municipio");
    private final TextField sector = new TextField("Sector");
    private final TextField streetAddress = new TextField("Dirección");
    private final TextArea referencePoints = new TextArea("Puntos de Referencia");

    // Additional Information
    private final ComboBox<ClientRating> rating = new ComboBox<>("Calificación");
    private final ComboBox<ReferenceSource> referenceSource = new ComboBox<>("Fuente de Referencia");
    private final TextArea notes = new TextArea("Notas");

    private final Button saveButton = new Button("Guardar");
    private final Button cancelButton = new Button("Cancelar");

    private final Binder<ClientCreateRequestDto> binder = new BeanValidationBinder<>(ClientCreateRequestDto.class);
    private final ClientService clientService;

    @Setter
    private Runnable onSaveCallback;

    private final List<Consumer<ClientCreateRequestDto>> clientSavedListeners = new ArrayList<>();
    private final List<Runnable> clientCancelledListeners = new ArrayList<>();

    public IndividualClientForm(ClientService clientService) {
        this.clientService = clientService;

        setHeaderTitle("Nuevo Cliente Individual");
        setModal(true);
        setWidth("900px");
        setHeight("80vh");

        createForm();
        setupValidation();
        setupEventListeners();
    }

    private void createForm() {
        FormLayout personalInfo = new FormLayout();
        personalInfo.add(firstName, lastName, email, phoneNumber, birthDate, gender, nationality);
        personalInfo.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        FormLayout identificationInfo = new FormLayout();
        identificationInfo.add(cedula, passport);
        identificationInfo.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        FormLayout contactInfo = new FormLayout();
        contactInfo.add(preferredContactMethod, emergencyContactName, emergencyContactNumber);
        contactInfo.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        FormLayout addressInfo = new FormLayout();
        addressInfo.add(province, municipality, sector, streetAddress);
        addressInfo.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        FormLayout additionalInfo = new FormLayout();
        additionalInfo.add(rating, referenceSource);
        additionalInfo.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Setup combo boxes
        gender.setItems(Gender.values());
        gender.setItemLabelGenerator(Gender::name);

        preferredContactMethod.setItems(PreferredContactMethod.values());
        preferredContactMethod.setItemLabelGenerator(PreferredContactMethod::name);

        rating.setItems(ClientRating.values());
        rating.setItemLabelGenerator(ClientRating::name);
        rating.setValue(ClientRating.BUENO); // Default value

        referenceSource.setItems(ReferenceSource.values());
        referenceSource.setItemLabelGenerator(ReferenceSource::name);

        // Set default values
        nationality.setValue("Dominicana");

        // Configure text areas
        referencePoints.setMaxLength(500);
        notes.setMaxLength(1000);

        VerticalLayout content = new VerticalLayout();
        content.add(new H3("Información Personal"), personalInfo, new H3("Identificación"), identificationInfo,
                new H3("Información de Contacto"), contactInfo, new H3("Dirección"), addressInfo, referencePoints,
                new H3("Información Adicional"), additionalInfo, notes);

        content.addClassNames(LumoUtility.Padding.MEDIUM);

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.addClassNames(LumoUtility.JustifyContent.END, LumoUtility.Gap.MEDIUM);

        add(content, buttonLayout);
    }

    private void setupValidation() {
        binder.forField(firstName).asRequired("El nombre es requerido").bind(ClientCreateRequestDto::firstName, null);

        binder.forField(lastName).asRequired("El apellido es requerido").bind(ClientCreateRequestDto::lastName, null);

        binder.forField(email).asRequired("El correo electrónico es requerido").bind(ClientCreateRequestDto::email,
                null);

        binder.forField(phoneNumber).asRequired("El teléfono es requerido").bind(ClientCreateRequestDto::phoneNumber,
                null);

        binder.forField(birthDate).bind(ClientCreateRequestDto::birthDate, null);

        binder.forField(gender).bind(ClientCreateRequestDto::gender, null);

        binder.forField(nationality).asRequired("La nacionalidad es requerida")
                .bind(ClientCreateRequestDto::nationality, null);

        binder.forField(cedula).bind(ClientCreateRequestDto::cedula, null);

        binder.forField(passport).bind(ClientCreateRequestDto::passport, null);

        binder.forField(preferredContactMethod).bind(ClientCreateRequestDto::preferredContactMethod, null);

        binder.forField(emergencyContactName).bind(ClientCreateRequestDto::emergencyContactName, null);

        binder.forField(emergencyContactNumber).bind(ClientCreateRequestDto::emergencyContactNumber, null);

        binder.forField(rating).bind(ClientCreateRequestDto::rating, null);

        binder.forField(referenceSource).bind(ClientCreateRequestDto::referenceSource, null);

        binder.forField(province).asRequired("La provincia es requerida").bind(ClientCreateRequestDto::province, null);

        binder.forField(municipality).asRequired("El municipio es requerido").bind(ClientCreateRequestDto::municipality,
                null);

        binder.forField(sector).asRequired("El sector es requerido").bind(ClientCreateRequestDto::sector, null);

        binder.forField(streetAddress).asRequired("La dirección es requerida")
                .bind(ClientCreateRequestDto::streetAddress, null);

        binder.forField(referencePoints).bind(ClientCreateRequestDto::referencePoints, null);

        binder.forField(notes).bind(ClientCreateRequestDto::notes, null);
    }

    private void setupEventListeners() {
        saveButton.addClickListener(this::save);
        cancelButton.addClickListener(e -> {
            fireClientCancelledEvent();
            close();
        });

        // Add validation for identification documents
        cedula.addValueChangeListener(e -> {
            if (!e.getValue().isEmpty()) {
                passport.clear();
            }
        });

        passport.addValueChangeListener(e -> {
            if (!e.getValue().isEmpty()) {
                cedula.clear();
            }
        });
    }

    private void save(ClickEvent<Button> event) {
        try {
            // Validate that at least one identification document is provided
            String cedulaValue = cedula.getValue();
            String passportValue = passport.getValue();

            if ((cedulaValue == null || cedulaValue.trim().isEmpty())
                    && (passportValue == null || passportValue.trim().isEmpty())) {
                NotificationUtils.error("Debe proporcionar cédula o pasaporte");
                return;
            }

            ClientCreateRequestDto dto = new ClientCreateRequestDto(email.getValue(), firstName.getValue(),
                    lastName.getValue(), phoneNumber.getValue(), birthDate.getValue(), gender.getValue(),
                    nationality.getValue(), cedulaValue, passportValue, null, // RNC is null for individual clients
                    null, // companyName is null for individual clients
                    preferredContactMethod.getValue(), emergencyContactName.getValue(),
                    emergencyContactNumber.getValue(), rating.getValue(), null, // creditLimit
                    null, // paymentTermsDays
                    notes.getValue(), referenceSource.getValue(), province.getValue(), municipality.getValue(),
                    sector.getValue(), streetAddress.getValue(), referencePoints.getValue());

            clientService.createClient(dto);
            NotificationUtils.success("Cliente individual creado exitosamente");

            // Fire the event with the created DTO
            fireClientSavedEvent(dto);

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

            close();
        } catch (Exception e) {
            log.error("Error creating individual client", e);
            NotificationUtils.error("Error al crear cliente: " + e.getMessage());
        }
    }

    public void openForNew() {
        // Clear all fields
        binder.readBean(null);
        firstName.focus();
        open();
    }

    /**
     * Adds a listener that will be called when a client is successfully saved.
     * 
     * @param listener
     *            Consumer that receives the saved client DTO
     */
    public void addClientSavedListener(Consumer<ClientCreateRequestDto> listener) {
        clientSavedListeners.add(listener);
    }

    /**
     * Adds a listener that will be called when the form is cancelled.
     * 
     * @param listener
     *            Runnable to execute on cancel
     */
    public void addClientCancelledListener(Runnable listener) {
        clientCancelledListeners.add(listener);
    }

    /**
     * Notifies all saved listeners that a client was successfully saved.
     * 
     * @param dto
     *            The saved client DTO
     */
    private void fireClientSavedEvent(ClientCreateRequestDto dto) {
        clientSavedListeners.forEach(listener -> listener.accept(dto));
    }

    /**
     * Notifies all cancelled listeners that the form was cancelled.
     */
    private void fireClientCancelledEvent() {
        clientCancelledListeners.forEach(Runnable::run);
    }
}
