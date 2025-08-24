package com.wornux.views.consultations;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.*;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.InvoiceStatus;
import com.wornux.data.enums.SystemRole;
import com.wornux.security.UserUtils;
import com.wornux.services.implementations.InvoiceService;
import com.wornux.services.interfaces.*;
import com.wornux.utils.NotificationUtils;
import com.wornux.views.pets.SelectPetDialog;
import com.wornux.views.services.OfferingForm;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/** Enhanced form for creating and editing consultations with offering and product integration */
@Slf4j
public class ConsultationsForm extends Dialog {

  private final TextField petName = new TextField("Mascota");
  private final Button selectPetButton = new Button("Seleccionar");
  private final SelectPetDialog selectPetDialog;
  private final ComboBox<Employee> veterinarianComboBox = new ComboBox<>("Veterinario");
  private final TextArea notesTextArea = new TextArea("Notas de Consulta");
  private final TextArea diagnosisTextArea = new TextArea("Diagnóstico");
  private final TextArea treatmentTextArea = new TextArea("Tratamiento");
  private final TextArea prescriptionTextArea = new TextArea("Prescripción");
  private final ComboBox<Offering> serviceComboBox = new ComboBox<>("Seleccionar Servicio");
  private final Button addServiceButton = new Button("Agregar Servicio", new Icon(VaadinIcon.PLUS));
  private final Grid<ServiceItem> servicesGrid = new Grid<>(ServiceItem.class, false);
  private final ComboBox<Product> productComboBox = new ComboBox<>("Seleccionar Producto");
  private final NumberField productQuantityField = new NumberField("Cantidad");
  private final Button addProductButton = new Button("Agregar Producto", new Icon(VaadinIcon.PLUS));
  private final Grid<ProductItem> productsGrid = new Grid<>(ProductItem.class, false);
  private final Span totalServicesSpan = new Span("$0.00");
  private final Span totalProductsSpan = new Span("$0.00");
  private final Span grandTotalSpan = new Span("$0.00");
  private final Button saveButton = new Button("Registrar Consulta");
  private final Button cancelButton = new Button("Cancelar");
  private final Button createServiceButton = new Button("Crear Nuevo Servicio");
  private final Binder<Consultation> binder = new Binder<>(Consultation.class);
  private final transient ConsultationService consultationService;
  private final transient EmployeeService employeeService;
  private final transient PetService petService;
  private final transient OfferingService offeringService;
  private final transient InvoiceService invoiceService;
  private final transient ProductService productService;
  private final List<ServiceItem> selectedServices = new ArrayList<>();
  private final List<ProductItem> selectedProducts = new ArrayList<>();
  private final OfferingForm offeringForm;
  private Pet selectedPet;
  private transient Consultation editingConsultation;
  private transient Invoice editingInvoice;
  @Setter private transient Consumer<Consultation> onSaveCallback;

  public ConsultationsForm(
      ConsultationService consultationService,
      EmployeeService employeeService,
      PetService petService,
      OfferingService offeringService,
      InvoiceService invoiceService,
      ProductService productService) {
    this.consultationService = consultationService;
    this.employeeService = employeeService;
    this.petService = petService;
    this.offeringService = offeringService;
    this.productService = productService;
    this.invoiceService = invoiceService;
    this.offeringForm = new OfferingForm(offeringService);

    this.selectPetDialog = new SelectPetDialog(petService);

    petName.setReadOnly(true);
    selectPetButton.addClickListener(e -> selectPetDialog.open());
    selectPetDialog.addPetSelectedListener(
        pet -> {
          selectedPet = pet;
          petName.setInvalid(false);
          petName.setValue(pet != null ? pet.getName() : "");
        });

    setHeaderTitle("Consulta Veterinaria");
    setModal(true);
    setWidth("1200px");
    setHeight("800px");

    createForm();
    setupValidation();
    setupEventListeners();
    loadComboBoxData();
    setupServiceForm();
  }

  private void createForm() {

    HorizontalLayout petPickerLayout = new HorizontalLayout(petName, selectPetButton);
    petPickerLayout.setAlignItems(FlexComponent.Alignment.END);
    petName.setWidthFull();

    veterinarianComboBox.setItemLabelGenerator(emp -> emp.getFirstName() + " " + emp.getLastName());
    veterinarianComboBox.setRequired(true);
    veterinarianComboBox.setWidthFull();

    notesTextArea.setRequired(true);
    notesTextArea.setHeight("100px");
    notesTextArea.setWidthFull();

    diagnosisTextArea.setHeight("80px");
    diagnosisTextArea.setWidthFull();

    treatmentTextArea.setHeight("80px");
    treatmentTextArea.setWidthFull();

    prescriptionTextArea.setHeight("80px");
    prescriptionTextArea.setWidthFull();

    serviceComboBox.setItemLabelGenerator(
        service -> service.getName() + " - $" + service.getPrice());
    serviceComboBox.setWidthFull();
    serviceComboBox.setPlaceholder("Buscar servicios médicos...");

    addServiceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

    productComboBox.setItemLabelGenerator(
        product -> product.getName() + " - $" + product.getSalesPrice());
    productComboBox.setWidthFull();
    productComboBox.setPlaceholder("Buscar productos de uso interno...");

    productQuantityField.setValue(1.0);
    productQuantityField.setMin(0.1);
    productQuantityField.setStep(0.1);
    productQuantityField.setWidth("120px");

    addProductButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

    FormLayout basicInfoLayout = new FormLayout();

    basicInfoLayout.add(petPickerLayout, veterinarianComboBox);
    basicInfoLayout.add(notesTextArea, 2);
    basicInfoLayout.add(diagnosisTextArea, treatmentTextArea);
    basicInfoLayout.add(prescriptionTextArea, 2);

    VerticalLayout content = new VerticalLayout();
    content.add(new H3("Información Básica"));
    content.add(basicInfoLayout);

    content.add(createServicesSection());
    content.add(createProductsSection());
    content.add(createTotalsSection());

    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
    buttonLayout.addClassNames(LumoUtility.JustifyContent.END, LumoUtility.Gap.MEDIUM);

    add(content, buttonLayout);
  }

  private VerticalLayout createServicesSection() {
    VerticalLayout section = new VerticalLayout();
    section.setSpacing(true);
    section.setPadding(false);

    H4 title = new H4("Servicios Médicos");

    HorizontalLayout serviceSelection = new HorizontalLayout();
    serviceSelection.add(serviceComboBox, createServiceButton, addServiceButton);
    serviceSelection.setAlignItems(FlexComponent.Alignment.END);
    serviceSelection.setWidthFull();

    configureServicesGrid();

    section.add(title, serviceSelection, servicesGrid);
    return section;
  }

  private VerticalLayout createProductsSection() {
    VerticalLayout section = new VerticalLayout();
    section.setSpacing(true);
    section.setPadding(false);

    H4 title = new H4("Productos de Uso Interno");

    HorizontalLayout productSelection = new HorizontalLayout();
    productSelection.add(productComboBox, productQuantityField, addProductButton);
    productSelection.setAlignItems(FlexComponent.Alignment.END);
    productSelection.setWidthFull();

    configureProductsGrid();

    section.add(title, productSelection, productsGrid);
    return section;
  }

  private void configureServicesGrid() {
    servicesGrid
        .addColumn(item -> item.getService().getName())
        .setHeader("Servicio")
        .setAutoWidth(true);
    servicesGrid
        .addColumn(item -> "$" + item.getService().getPrice())
        .setHeader("Precio")
        .setAutoWidth(true);
    servicesGrid.addColumn(ServiceItem::getQuantity).setHeader("Cantidad").setAutoWidth(true);
    servicesGrid
        .addColumn(item -> "$" + item.getSubtotal())
        .setHeader("Subtotal")
        .setAutoWidth(true);

    servicesGrid
        .addComponentColumn(this::createServiceRemoveButton)
        .setHeader("Acciones")
        .setAutoWidth(true);

    servicesGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
    servicesGrid.setHeight("200px");
  }

  private void configureProductsGrid() {
    productsGrid
        .addColumn(item -> item.getProduct().getName())
        .setHeader("Producto")
        .setAutoWidth(true);
    productsGrid
        .addColumn(item -> "$" + item.getProduct().getSalesPrice())
        .setHeader("Precio")
        .setAutoWidth(true);
    productsGrid.addColumn(ProductItem::getQuantity).setHeader("Cantidad").setAutoWidth(true);
    productsGrid
        .addColumn(item -> "$" + item.getSubtotal())
        .setHeader("Subtotal")
        .setAutoWidth(true);

    productsGrid
        .addComponentColumn(this::createProductRemoveButton)
        .setHeader("Acciones")
        .setAutoWidth(true);

    productsGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
    productsGrid.setHeight("200px");
  }

  private HorizontalLayout createTotalsSection() {
    totalServicesSpan.addClassNames(LumoUtility.FontWeight.BOLD);
    totalProductsSpan.addClassNames(LumoUtility.FontWeight.BOLD);
    grandTotalSpan.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.LARGE);

    VerticalLayout totalsLayout = new VerticalLayout();
    totalsLayout.add(
        new HorizontalLayout(new Span("Total Servicios:"), totalServicesSpan),
        new HorizontalLayout(new Span("Total Productos:"), totalProductsSpan),
        new HorizontalLayout(new Span("Total General:"), grandTotalSpan));
    totalsLayout.setPadding(false);
    totalsLayout.setSpacing(false);

    HorizontalLayout section = new HorizontalLayout();
    section.add(new Div(), totalsLayout);
    section.setWidthFull();
    section.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);

    return section;
  }

  private Button createServiceRemoveButton(ServiceItem item) {
    Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
    removeButton.addThemeVariants(
        ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
    removeButton.addClickListener(
        e -> {
          selectedServices.remove(item);
          servicesGrid.setItems(selectedServices);
          updateTotals();
        });
    return removeButton;
  }

  private Button createProductRemoveButton(ProductItem item) {
    Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
    removeButton.addThemeVariants(
        ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
    removeButton.addClickListener(
        e -> {
          selectedProducts.remove(item);
          productsGrid.setItems(selectedProducts);
          updateTotals();
        });
    return removeButton;
  }

  private void setupValidation() {

    binder
        .forField(veterinarianComboBox)
        .withValidator(vet -> vet != null, "Debe seleccionar un veterinario")
        .bind(Consultation::getVeterinarian, Consultation::setVeterinarian);

    binder
        .forField(notesTextArea)
        .withValidator(
            notes -> notes != null && !notes.trim().isEmpty(), "Las notas son requeridas")
        .bind(Consultation::getNotes, Consultation::setNotes);

    binder.forField(diagnosisTextArea).bind(Consultation::getDiagnosis, Consultation::setDiagnosis);

    binder.forField(treatmentTextArea).bind(Consultation::getTreatment, Consultation::setTreatment);

    binder
        .forField(prescriptionTextArea)
        .bind(Consultation::getPrescription, Consultation::setPrescription);
  }

  private void setupEventListeners() {
    addServiceButton.addClickListener(e -> addSelectedService());
    serviceComboBox.addValueChangeListener(e -> addServiceButton.setEnabled(e.getValue() != null));

    addProductButton.addClickListener(e -> addSelectedProduct());
    productComboBox.addValueChangeListener(
        e ->
            addProductButton.setEnabled(
                e.getValue() != null
                    && productQuantityField.getValue() != null
                    && productQuantityField.getValue() > 0));

    createServiceButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    createServiceButton.setIcon(VaadinIcon.PLUS.create());
    createServiceButton.addClickListener(e -> openServiceCreationDialog());

    saveButton.addClickListener(this::save);
    cancelButton.addClickListener(e -> close());
  }

  private void addSelectedService() {
    Offering selectedOffering = serviceComboBox.getValue();
    if (selectedOffering == null) return;

    // Check if offering already added
    boolean alreadyAdded =
        selectedServices.stream()
            .anyMatch(item -> item.getService().getId().equals(selectedOffering.getId()));

    if (alreadyAdded) {
      NotificationUtils.error("Este servicio ya ha sido agregado");
      return;
    }

    ServiceItem serviceItem = new ServiceItem(selectedOffering, 1.0);
    selectedServices.add(serviceItem);
    servicesGrid.setItems(selectedServices);
    serviceComboBox.clear();
    updateTotals();
  }

  private void openServiceCreationDialog() {
    offeringForm.openForNew();
  }

  private void addSelectedProduct() {
    Product selectedProduct = productComboBox.getValue();
    Double quantity = productQuantityField.getValue();

    if (selectedProduct == null || quantity == null || quantity <= 0) return;

    boolean alreadyAdded =
        selectedProducts.stream()
            .anyMatch(item -> item.getProduct().getId().equals(selectedProduct.getId()));

    if (alreadyAdded) {
      NotificationUtils.error("Este producto ya ha sido agregado");
      return;
    }

    ProductItem productItem = new ProductItem(selectedProduct, quantity);
    selectedProducts.add(productItem);
    productsGrid.setItems(selectedProducts);
    productComboBox.clear();
    productQuantityField.setValue(1.0);
    updateTotals();
  }

  private void updateTotals() {
    BigDecimal servicesTotal =
        selectedServices.stream()
            .map(ServiceItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal productsTotal =
        selectedProducts.stream()
            .map(ProductItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal grandTotal = servicesTotal.add(productsTotal);

    totalServicesSpan.setText("$" + servicesTotal);
    totalProductsSpan.setText("$" + productsTotal);
    grandTotalSpan.setText("$" + grandTotal);
  }

  private void loadComboBoxData() {
    veterinarianComboBox.setItems(employeeService.getVeterinarians());

    serviceComboBox.setItems(offeringService.findMedicalServices());

    productComboBox.setItems(productService.findInternalUseProducts());
  }

  private void save(ClickEvent<Button> event) {
    try {
      boolean isNew = editingConsultation == null;
      if (isNew) {
        editingConsultation = new Consultation();
        editingConsultation.setConsultationDate(LocalDateTime.now());
      }

      if (!isNew && !editingConsultation.isActive()) {
        if (UserUtils.hasEmployeeRole(EmployeeRole.CLINIC_MANAGER)
            || UserUtils.hasSystemRole(SystemRole.SYSTEM_ADMIN)) {
          editingConsultation.setActive(true);
        }
      }

      if (selectedPet == null) {
        petName.setInvalid(true);
        petName.setErrorMessage("Debe seleccionar una mascota");
        NotificationUtils.error("Debe seleccionar una mascota");
        return;
      }

      binder.writeBean(editingConsultation);

      editingConsultation.setPet(selectedPet);

      Consultation savedConsultation = consultationService.save(editingConsultation);

      saveOrUpdateInvoice(savedConsultation);

      NotificationUtils.success("Consulta registrada exitosamente");

      if (onSaveCallback != null) {
        onSaveCallback.accept(savedConsultation);
      }
      close();

    } catch (ValidationException e) {
      NotificationUtils.error("Por favor, complete todos los campos requeridos");
    } catch (ObjectOptimisticLockingFailureException ex) {
      log.error(ex.getLocalizedMessage());
      NotificationUtils.error(
          "Error al actualizar los datos. Alguien más ha actualizado el registro mientras realizabas cambios.");
    } catch (Exception e) {
      NotificationUtils.error("Error al guardar la consulta: " + e.getMessage());
    }
  }

  public void openForNew() {
    clearForm();

    selectedPet = null;
    petName.clear();
    petName.setInvalid(false);

    saveButton.setText("Registrar Consulta");
    editingConsultation = null;
    editingInvoice = null;
    setHeaderTitle("Nueva Consulta");
    open();
  }

  public void openForEdit(Consultation consultation) {
    clearForm();
    this.editingConsultation = consultation;
    binder.readBean(consultation);
    selectedPet = consultation.getPet();
    petName.setValue(selectedPet != null ? selectedPet.getName() : "");
    petName.setInvalid(false);

    invoiceService
        .getRepository()
        .findByConsultation(consultation)
        .ifPresent(
            invoice -> {
              this.editingInvoice = invoice;

              invoice
                  .getOfferings()
                  .forEach(
                      serviceInvoice ->
                          selectedServices.add(
                              new ServiceItem(
                                  serviceInvoice.getOffering(), serviceInvoice.getQuantity())));

              invoice
                  .getProducts()
                  .forEach(
                      invoiceProduct ->
                          selectedProducts.add(
                              new ProductItem(
                                  invoiceProduct.getProduct(), invoiceProduct.getQuantity())));

              servicesGrid.setItems(selectedServices);
              productsGrid.setItems(selectedProducts);
              updateTotals();
            });

    saveButton.setText("Actualizar Consulta");
    setHeaderTitle("Editar Consulta");
    open();
  }

  private void clearForm() {
    binder.readBean(null);
    selectedServices.clear();
    selectedProducts.clear();
    servicesGrid.setItems(selectedServices);
    productsGrid.setItems(selectedProducts);
    updateTotals();
    serviceComboBox.clear();
    productComboBox.clear();
    productQuantityField.setValue(1.0);
    selectedPet = null;
    petName.clear();
    editingInvoice = null;
  }

  private void setupServiceForm() {
    offeringForm.addServiceSavedListener(
        dto -> {
          var medicalServices = offeringService.findMedicalServices();
          serviceComboBox.setItems(medicalServices);
          medicalServices.stream()
              .filter(s -> s.getName().equalsIgnoreCase(dto.getName()))
              .findFirst()
              .ifPresent(serviceComboBox::setValue);
        });
  }

  private void saveOrUpdateInvoice(Consultation consultation) {
    if (selectedServices.isEmpty() && selectedProducts.isEmpty()) {
      if (editingInvoice != null) {
        if (editingInvoice.getStatus() == InvoiceStatus.PENDING) {
          try {
            editingInvoice.setStatus(InvoiceStatus.CANCELLED);
            invoiceService.delete(editingInvoice.getCode());
            NotificationUtils.info("Factura asociada pendiente anulada.");
          } catch (Exception e) {
            log.error("Error anulando factura pendiente: {}", editingInvoice.getCode(), e);
            NotificationUtils.error("Error al anular la factura asociada pendiente.");
          }
        } else {
          NotificationUtils.info(
              "Factura asociada pagada. No se realizaron cambios en la factura.");
        }
      }
      return;
    }

    Invoice invoiceToSave;
    if (editingInvoice != null) {
      if (editingInvoice.getStatus() == InvoiceStatus.PAID) {
        NotificationUtils.info("Factura asociada pagada. No se realizaron cambios en la factura.");
        return;
      } else {
        try {
          editingInvoice.setStatus(InvoiceStatus.CANCELLED);
          invoiceService.delete(editingInvoice.getCode());
          NotificationUtils.info(" Factura previa anulada, se ha creado una nueva.");
        } catch (Exception e) {
          log.error("Error anulando factura pendiente anterior: {}", editingInvoice.getCode(), e);
          NotificationUtils.error("Error al anular la factura asociada pendiente anterior.");
          return;
        }
        invoiceToSave =
            Invoice.builder()
                .client(consultation.getPet().getOwners().iterator().next())
                .consultation(consultation)
                .issuedDate(LocalDate.now())
                .paymentDate(LocalDate.now().plusDays(30))
                .status(InvoiceStatus.PENDING)
                .paidToDate(BigDecimal.ZERO)
                .active(true)
                .build();
      }
    } else {
      invoiceToSave =
          Invoice.builder()
              .client(consultation.getPet().getOwners().iterator().next())
              .consultation(consultation)
              .issuedDate(LocalDate.now())
              .paymentDate(LocalDate.now().plusDays(30))
              .status(InvoiceStatus.PENDING)
              .paidToDate(BigDecimal.ZERO)
              .active(true)
              .build();
    }

    invoiceToSave.getOfferings().clear();
    for (ServiceItem serviceItem : selectedServices) {
      InvoiceOffering serviceInvoice =
          InvoiceOffering.builder()
              .offering(serviceItem.getService())
              .quantity(serviceItem.getQuantity())
              .amount(serviceItem.getSubtotal())
              .build();
      invoiceToSave.addOffering(serviceInvoice);
    }

    invoiceToSave.getProducts().clear();
    for (ProductItem productItem : selectedProducts) {
      InvoiceProduct invoiceProduct =
          InvoiceProduct.builder()
              .product(productItem.getProduct())
              .quantity(productItem.getQuantity())
              .price(productItem.getProduct().getSalesPrice())
              .amount(productItem.getSubtotal())
              .build();
      invoiceToSave.addProduct(invoiceProduct);
    }

    invoiceToSave.calculateTotals();
    invoiceToSave.setConsultationNotes(consultation.getNotes());

    try {
      invoiceService.create(invoiceToSave);
      NotificationUtils.success("Factura guardada automáticamente.");
    } catch (Exception e) {
      log.error("Error al guardar la factura para la consulta: {}", consultation.getId(), e);
      NotificationUtils.error("Error al generar/actualizar la factura: " + e.getMessage());
    }
  }

  public static class ServiceItem {
    private final Offering offering;
    private final Double quantity;
    private final BigDecimal subtotal;

    public ServiceItem(Offering offering, Double quantity) {
      this.offering = offering;
      this.quantity = quantity;
      this.subtotal = offering.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Offering getService() {
      return offering;
    }

    public Double getQuantity() {
      return quantity;
    }

    public BigDecimal getSubtotal() {
      return subtotal;
    }
  }

  public static class ProductItem {
    private final Product product;
    private final Double quantity;
    private final BigDecimal subtotal;

    public ProductItem(Product product, Double quantity) {
      this.product = product;
      this.quantity = quantity;
      this.subtotal = product.getSalesPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Product getProduct() {
      return product;
    }

    public Double getQuantity() {
      return quantity;
    }

    public BigDecimal getSubtotal() {
      return subtotal;
    }
  }
}
