package com.wornux.views.clients;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.enums.*;
import com.wornux.dto.request.ClientCreateRequestDto;
import com.wornux.services.interfaces.ClientService;
import com.wornux.utils.NotificationUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompanyClientForm extends Dialog {

  // Company Information
  private final TextField companyName = new TextField("Nombre de la Empresa");
  private final TextField rnc = new TextField("RNC");
  private final EmailField email = new EmailField("Correo Electrónico");
  private final TextField phoneNumber = new TextField("Teléfono");

  // Contact Information
  private final ComboBox<PreferredContactMethod> preferredContactMethod =
      new ComboBox<>("Método de Contacto Preferido");
  private final TextField emergencyContactName = new TextField("Nombre del Contacto de Emergencia");
  private final TextField emergencyContactNumber = new TextField("Teléfono de Emergencia");

  // Address Information
  private final TextField province = new TextField("Provincia");
  private final TextField municipality = new TextField("Municipio");
  private final TextField sector = new TextField("Sector");
  private final TextField streetAddress = new TextField("Dirección");
  private final TextArea referencePoints = new TextArea("Puntos de Referencia");

  // Business Information
  private final NumberField creditLimit = new NumberField("Límite de Crédito");
  private final NumberField paymentTermsDays = new NumberField("Días de Términos de Pago");
  private final ComboBox<ClientRating> rating = new ComboBox<>("Calificación");
  private final ComboBox<ReferenceSource> referenceSource = new ComboBox<>("Fuente de Referencia");
  private final TextArea notes = new TextArea("Notas");

  private final Button saveButton = new Button("Guardar");
  private final Button cancelButton = new Button("Cancelar");

  private final Binder<ClientCreateRequestDto> binder =
      new BeanValidationBinder<>(ClientCreateRequestDto.class);
  private final ClientService clientService;
  private final List<Consumer<ClientCreateRequestDto>> clientSavedListeners = new ArrayList<>();
  private final List<Runnable> clientCancelledListeners = new ArrayList<>();
  @Setter private Runnable onSaveCallback;

  public CompanyClientForm(ClientService clientService) {
    this.clientService = clientService;

    setHeaderTitle("Nueva Empresa");
    setModal(true);
    setWidth("900px");
    setMaxWidth("95vw");
    setHeight("85vh");
    setMaxHeight("95vh");

    // Add header styling similar to ClientForm
    getHeader()
        .getElement()
        .getStyle()
        .set("background", "var(--lumo-primary-color-10pct)")
        .set("color", "var(--lumo-primary-text-color)");

    createForm();
    setupValidation();
    setupEventListeners();
  }

  private void createForm() {
    FormLayout companyInfo = new FormLayout();
    companyInfo.add(companyName, rnc, email, phoneNumber);
    companyInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    FormLayout contactInfo = new FormLayout();
    contactInfo.add(preferredContactMethod, emergencyContactName, emergencyContactNumber);
    contactInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    FormLayout addressInfo = new FormLayout();
    addressInfo.add(province, municipality, sector, streetAddress);
    addressInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    FormLayout businessInfo = new FormLayout();
    businessInfo.add(creditLimit, paymentTermsDays, rating, referenceSource);
    businessInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    // Setup combo boxes
    preferredContactMethod.setItems(PreferredContactMethod.values());
    preferredContactMethod.setItemLabelGenerator(PreferredContactMethod::name);

    rating.setItems(ClientRating.values());
    rating.setItemLabelGenerator(ClientRating::name);
    rating.setValue(ClientRating.BUENO); // Default value

    referenceSource.setItems(ReferenceSource.values());
    referenceSource.setItemLabelGenerator(ReferenceSource::name);

    // Configure number fields
    creditLimit.setMin(0);
    creditLimit.setValue(0.0);
    creditLimit.setSuffixComponent(new TextField().getPrefixComponent());

    paymentTermsDays.setMin(0);
    paymentTermsDays.setValue(0.0);
    paymentTermsDays.setStep(1);

    // Configure text areas
    referencePoints.setMaxLength(500);
    notes.setMaxLength(1000);

    // Configure RNC field
    rnc.setPlaceholder("Ej: 123456789");
    rnc.setHelperText("9 dígitos");

    // Add icons to fields
    companyName.setPrefixComponent(VaadinIcon.BUILDING.create());
    rnc.setPrefixComponent(VaadinIcon.CREDIT_CARD.create());
    email.setPrefixComponent(VaadinIcon.ENVELOPE.create());
    phoneNumber.setPrefixComponent(VaadinIcon.PHONE.create());
    preferredContactMethod.setPrefixComponent(VaadinIcon.CONNECT.create());
    emergencyContactName.setPrefixComponent(VaadinIcon.USERS.create());
    emergencyContactNumber.setPrefixComponent(VaadinIcon.PHONE_LANDLINE.create());
    province.setPrefixComponent(VaadinIcon.LOCATION_ARROW.create());
    municipality.setPrefixComponent(VaadinIcon.HOME.create());
    sector.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
    streetAddress.setPrefixComponent(VaadinIcon.ROAD.create());
    referencePoints.setPrefixComponent(VaadinIcon.INFO_CIRCLE.create());
    creditLimit.setPrefixComponent(VaadinIcon.DOLLAR.create());
    paymentTermsDays.setPrefixComponent(VaadinIcon.CALENDAR_CLOCK.create());
    rating.setPrefixComponent(VaadinIcon.STAR.create());
    referenceSource.setPrefixComponent(VaadinIcon.QUESTION_CIRCLE.create());
    notes.setPrefixComponent(VaadinIcon.EDIT.create());

    VerticalLayout content = new VerticalLayout();
    content.add(
        new H3("Información de la Empresa"),
        companyInfo,
        new H3("Información de Contacto"),
        contactInfo,
        new H3("Dirección"),
        addressInfo,
        referencePoints,
        new H3("Información Comercial"),
        businessInfo,
        notes);

    content.addClassNames(LumoUtility.Padding.MEDIUM);

    HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
    buttonLayout.addClassNames(LumoUtility.JustifyContent.END, LumoUtility.Gap.MEDIUM);

    add(content, buttonLayout);
  }

  private void setupValidation() {
    binder
        .forField(companyName)
        .asRequired("El nombre de la empresa es requerido")
        .bind(ClientCreateRequestDto::companyName, null);

    binder
        .forField(rnc)
        .asRequired("El RNC es requerido")
        .withValidator(
            value -> value != null && value.matches("\\d{9}"),
            "El RNC debe contener exactamente 9 dígitos")
        .bind(ClientCreateRequestDto::rnc, null);

    binder
        .forField(email)
        .asRequired("El correo electrónico es requerido")
        .bind(ClientCreateRequestDto::email, null);

    binder
        .forField(phoneNumber)
        .asRequired("El teléfono es requerido")
        .bind(ClientCreateRequestDto::phoneNumber, null);

    binder
        .forField(preferredContactMethod)
        .bind(ClientCreateRequestDto::preferredContactMethod, null);

    binder.forField(emergencyContactName).bind(ClientCreateRequestDto::emergencyContactName, null);

    binder
        .forField(emergencyContactNumber)
        .bind(ClientCreateRequestDto::emergencyContactNumber, null);

    binder.forField(rating).bind(ClientCreateRequestDto::rating, null);

    binder.forField(referenceSource).bind(ClientCreateRequestDto::referenceSource, null);

    binder
        .forField(province)
        .asRequired("La provincia es requerida")
        .bind(ClientCreateRequestDto::province, null);

    binder
        .forField(municipality)
        .asRequired("El municipio es requerido")
        .bind(ClientCreateRequestDto::municipality, null);

    binder
        .forField(sector)
        .asRequired("El sector es requerido")
        .bind(ClientCreateRequestDto::sector, null);

    binder
        .forField(streetAddress)
        .asRequired("La dirección es requerida")
        .bind(ClientCreateRequestDto::streetAddress, null);

    binder.forField(referencePoints).bind(ClientCreateRequestDto::referencePoints, null);

    binder.forField(notes).bind(ClientCreateRequestDto::notes, null);
  }

  private void setupEventListeners() {
    saveButton.addClickListener(this::save);
    cancelButton.addClickListener(
        e -> {
          fireClientCancelledEvent();
          close();
        });
  }

  private void save(ClickEvent<Button> event) {
    try {
      // Validate RNC
      String rncValue = rnc.getValue();
      if (rncValue == null || rncValue.trim().isEmpty() || !rncValue.matches("\\d{9}")) {
        NotificationUtils.error("Debe proporcionar un RNC válido de 9 dígitos");
        return;
      }

      ClientCreateRequestDto dto =
          new ClientCreateRequestDto(
              email.getValue(),
              null,
              // firstName is null for companies
              null, // lastName is null for companies
              phoneNumber.getValue(),
              null, // birthDate is null for companies
              null, // gender is null for companies
              null, // nationality is null for companies
              null, // cedula is null for companies
              null, // passport is null for companies
              rncValue,
              companyName.getValue(),
              preferredContactMethod.getValue(),
              emergencyContactName.getValue(),
              emergencyContactNumber.getValue(),
              rating.getValue(),
              creditLimit.getValue(),
              paymentTermsDays.getValue().intValue(),
              notes.getValue(),
              referenceSource.getValue(),
              province.getValue(),
              municipality.getValue(),
              sector.getValue(),
              streetAddress.getValue(),
              referencePoints.getValue());

      clientService.createClient(dto);
      NotificationUtils.success("Empresa creada exitosamente");

      // Fire the event with the created DTO
      fireClientSavedEvent(dto);

      if (onSaveCallback != null) {
        onSaveCallback.run();
      }

      close();
    } catch (Exception e) {
      log.error("Error creating company client", e);
      NotificationUtils.error("Error al crear empresa: " + e.getMessage());
    }
  }

  public void openForNew() {
    // Clear all fields
    binder.readBean(null);
    companyName.focus();
    open();
  }

  /**
   * Adds a listener that will be called when a client is successfully saved.
   *
   * @param listener Consumer that receives the saved client DTO
   */
  public void addClientSavedListener(Consumer<ClientCreateRequestDto> listener) {
    clientSavedListeners.add(listener);
  }

  /**
   * Adds a listener that will be called when the form is cancelled.
   *
   * @param listener Runnable to execute on cancel
   */
  public void addClientCancelledListener(Runnable listener) {
    clientCancelledListeners.add(listener);
  }

  /**
   * Notifies all saved listeners that a client was successfully saved.
   *
   * @param dto The saved client DTO
   */
  private void fireClientSavedEvent(ClientCreateRequestDto dto) {
    clientSavedListeners.forEach(listener -> listener.accept(dto));
  }

  /** Notifies all cancelled listeners that the form was cancelled. */
  private void fireClientCancelledEvent() {
    clientCancelledListeners.forEach(Runnable::run);
  }
}
