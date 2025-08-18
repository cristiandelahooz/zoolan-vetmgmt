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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.*;
import com.wornux.data.enums.InvoiceStatus;
import com.wornux.services.implementations.InvoiceService;
import com.wornux.services.interfaces.*;
import com.wornux.utils.NotificationUtils;
import com.wornux.views.services.ServiceForm;
import lombok.Setter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Enhanced form for creating and editing consultations with service and product integration
 */
public class ConsultationsForm extends Dialog {

  // Form fields
  private final ComboBox<Pet> petComboBox = new ComboBox<>("Mascota");
  private final ComboBox<Employee> veterinarianComboBox = new ComboBox<>("Veterinario");
  private final TextArea notesTextArea = new TextArea("Notas de Consulta");
  private final TextArea diagnosisTextArea = new TextArea("Diagnóstico");
  private final TextArea treatmentTextArea = new TextArea("Tratamiento");
  private final TextArea prescriptionTextArea = new TextArea("Prescripción");

  // Service selection components
  private final ComboBox<Service> serviceComboBox = new ComboBox<>("Seleccionar Servicio");
  private final Button addServiceButton = new Button("Agregar Servicio", new Icon(VaadinIcon.PLUS));
  private final Grid<ServiceItem> servicesGrid = new Grid<>(ServiceItem.class, false);

  // Product selection components
  private final ComboBox<Product> productComboBox = new ComboBox<>("Seleccionar Producto");
  private final NumberField productQuantityField = new NumberField("Cantidad");
  private final Button addProductButton = new Button("Agregar Producto", new Icon(VaadinIcon.PLUS));
  private final Grid<ProductItem> productsGrid = new Grid<>(ProductItem.class, false);

  // Totals
  private final Span totalServicesSpan = new Span("$0.00");
  private final Span totalProductsSpan = new Span("$0.00");
  private final Span grandTotalSpan = new Span("$0.00");

  // Form controls
  private final Button saveButton = new Button("Registrar Consulta");
  private final Button cancelButton = new Button("Cancelar");
  private final Button createServiceButton = new Button("Crear Nuevo Servicio");

  private final Binder<Consultation> binder = new Binder<>(Consultation.class);

  // Services
  private final transient ConsultationService consultationService;
  private final transient EmployeeService employeeService;
  private final transient PetService petService;
  private final transient ServiceService serviceService;
  private final transient InvoiceService invoiceService;
  private final transient ProductService productService;

  // Data
  private transient Consultation editingConsultation;
  private final List<ServiceItem> selectedServices = new ArrayList<>();
  private final List<ProductItem> selectedProducts = new ArrayList<>();
  private final ServiceForm serviceForm;

  @Setter
  private transient Consumer<Consultation> onSaveCallback;

  public ConsultationsForm(ConsultationService consultationService,
                           EmployeeService employeeService,
                           PetService petService,
                           ServiceService serviceService,
                           InvoiceService invoiceService,
                           ProductService productService) {
    this.consultationService = consultationService;
    this.employeeService = employeeService;
    this.petService = petService;
    this.serviceService = serviceService;
    this.productService = productService;
    this.invoiceService = invoiceService;
    this.serviceForm = new ServiceForm(serviceService);

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
    // Configure basic fields
    petComboBox.setItemLabelGenerator(pet -> pet.getName() + " (" + pet.getType() + ")");
    petComboBox.setRequired(true);
    petComboBox.setWidthFull();

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

    // Configure service selection
    serviceComboBox.setItemLabelGenerator(service -> service.getName() + " - $" + service.getPrice());
    serviceComboBox.setWidthFull();
    serviceComboBox.setPlaceholder("Buscar servicios médicos...");

    addServiceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

    // Configure product selection
    productComboBox.setItemLabelGenerator(product -> product.getName() + " - $" + product.getSalesPrice());
    productComboBox.setWidthFull();
    productComboBox.setPlaceholder("Buscar productos de uso interno...");

    productQuantityField.setValue(1.0);
    productQuantityField.setMin(0.1);
    productQuantityField.setStep(0.1);
    productQuantityField.setWidth("120px");

    addProductButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

    // Create layout
    FormLayout basicInfoLayout = new FormLayout();
    basicInfoLayout.add(petComboBox, veterinarianComboBox);
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
    servicesGrid.addColumn(item -> item.getService().getName()).setHeader("Servicio").setAutoWidth(true);
    servicesGrid.addColumn(item -> "$" + item.getService().getPrice()).setHeader("Precio").setAutoWidth(true);
    servicesGrid.addColumn(ServiceItem::getQuantity).setHeader("Cantidad").setAutoWidth(true);
    servicesGrid.addColumn(item -> "$" + item.getSubtotal()).setHeader("Subtotal").setAutoWidth(true);

    servicesGrid.addComponentColumn(this::createServiceRemoveButton).setHeader("Acciones").setAutoWidth(true);

    servicesGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
    servicesGrid.setHeight("200px");
  }

  private void configureProductsGrid() {
    productsGrid.addColumn(item -> item.getProduct().getName()).setHeader("Producto").setAutoWidth(true);
    productsGrid.addColumn(item -> "$" + item.getProduct().getSalesPrice()).setHeader("Precio").setAutoWidth(true);
    productsGrid.addColumn(ProductItem::getQuantity).setHeader("Cantidad").setAutoWidth(true);
    productsGrid.addColumn(item -> "$" + item.getSubtotal()).setHeader("Subtotal").setAutoWidth(true);

    productsGrid.addComponentColumn(this::createProductRemoveButton).setHeader("Acciones").setAutoWidth(true);

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
        new HorizontalLayout(new Span("Total General:"), grandTotalSpan)
    );
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
    removeButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
    removeButton.addClickListener(e -> {
      selectedServices.remove(item);
      servicesGrid.setItems(selectedServices);
      updateTotals();
    });
    return removeButton;
  }

  private Button createProductRemoveButton(ProductItem item) {
    Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
    removeButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
    removeButton.addClickListener(e -> {
      selectedProducts.remove(item);
      productsGrid.setItems(selectedProducts);
      updateTotals();
    });
    return removeButton;
  }

  private void setupValidation() {
    binder.forField(petComboBox)
        .withValidator(pet -> pet != null, "Debe seleccionar una mascota")
        .bind(Consultation::getPet, Consultation::setPet);

    binder.forField(veterinarianComboBox)
        .withValidator(vet -> vet != null, "Debe seleccionar un veterinario")
        .bind(Consultation::getVeterinarian, Consultation::setVeterinarian);

    binder.forField(notesTextArea)
        .withValidator(notes -> notes != null && !notes.trim().isEmpty(), "Las notas son requeridas")
        .bind(Consultation::getNotes, Consultation::setNotes);

    binder.forField(diagnosisTextArea)
        .bind(Consultation::getDiagnosis, Consultation::setDiagnosis);

    binder.forField(treatmentTextArea)
        .bind(Consultation::getTreatment, Consultation::setTreatment);

    binder.forField(prescriptionTextArea)
        .bind(Consultation::getPrescription, Consultation::setPrescription);
  }

  private void setupEventListeners() {
    addServiceButton.addClickListener(e -> addSelectedService());
    serviceComboBox.addValueChangeListener(e -> addServiceButton.setEnabled(e.getValue() != null));

    addProductButton.addClickListener(e -> addSelectedProduct());
    productComboBox.addValueChangeListener(e ->
        addProductButton.setEnabled(e.getValue() != null && productQuantityField.getValue() != null && productQuantityField.getValue() > 0)
    );

    createServiceButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    createServiceButton.setIcon(VaadinIcon.PLUS.create());
    createServiceButton.addClickListener(e -> openServiceCreationDialog());

    saveButton.addClickListener(this::save);
    cancelButton.addClickListener(e -> close());
  }

  private void addSelectedService() {
    Service selectedService = serviceComboBox.getValue();
    if (selectedService == null) return;

    // Check if service already added
    boolean alreadyAdded = selectedServices.stream()
        .anyMatch(item -> item.getService().getId().equals(selectedService.getId()));

    if (alreadyAdded) {
      NotificationUtils.error("Este servicio ya ha sido agregado");
      return;
    }

    ServiceItem serviceItem = new ServiceItem(selectedService, 1.0);
    selectedServices.add(serviceItem);
    servicesGrid.setItems(selectedServices);
    serviceComboBox.clear();
    updateTotals();
  }

  private void openServiceCreationDialog() {
    serviceForm.openForNew();
  }

  private void addSelectedProduct() {
    Product selectedProduct = productComboBox.getValue();
    Double quantity = productQuantityField.getValue();

    if (selectedProduct == null || quantity == null || quantity <= 0) return;

    // Check if product already added
    boolean alreadyAdded = selectedProducts.stream()
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
    BigDecimal servicesTotal = selectedServices.stream()
        .map(ServiceItem::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal productsTotal = selectedProducts.stream()
        .map(ProductItem::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal grandTotal = servicesTotal.add(productsTotal);

    totalServicesSpan.setText("$" + servicesTotal);
    totalProductsSpan.setText("$" + productsTotal);
    grandTotalSpan.setText("$" + grandTotal);
  }

  private void loadComboBoxData() {
    // Load pets
    petService.getAllPets().forEach(pet -> petComboBox.getListDataView().addItem(pet));

    // Load veterinarians
    employeeService.getVeterinarians()
        .forEach(emp -> veterinarianComboBox.getListDataView().addItem(emp));

    // Load medical services
    serviceService.findMedicalServices()
        .forEach(service -> serviceComboBox.getListDataView().addItem(service));

    // Load internal use products
    productService.findInternalUseProducts()
        .forEach(product -> productComboBox.getListDataView().addItem(product));
  }

  private void save(ClickEvent<Button> event) {
    try {
      if (editingConsultation == null) {
        editingConsultation = new Consultation();
        editingConsultation.setConsultationDate(LocalDateTime.now());
      }

      binder.writeBean(editingConsultation);

      // Save consultation
      Consultation savedConsultation = consultationService.save(editingConsultation);

      // Create automatic invoice if services or products are selected
      if (!selectedServices.isEmpty() || !selectedProducts.isEmpty()) {
        createAutomaticInvoice(savedConsultation);
      }

      NotificationUtils.success("Consulta registrada exitosamente");

      if (onSaveCallback != null) {
        onSaveCallback.accept(savedConsultation);
      }
      close();

    } catch (ValidationException e) {
      NotificationUtils.error("Por favor, complete todos los campos requeridos");
    } catch (Exception e) {
      NotificationUtils.error("Error al guardar la consulta: " + e.getMessage());
    }
  }

  private void createAutomaticInvoice(Consultation consultation) {
    try {
      // Create invoice
      Invoice invoice = Invoice.builder()
          .client(consultation.getPet().getOwners().iterator().next())
          .consultation(consultation)
          .issuedDate(LocalDate.now())
          .paymentDate(LocalDate.now().plusDays(30))
          .status(InvoiceStatus.PENDING)
          .consultationNotes(consultation.getNotes())
          .subtotal(BigDecimal.ZERO)
          .tax(BigDecimal.ZERO)
          .total(BigDecimal.ZERO)
          .paidToDate(BigDecimal.ZERO)
          .build();

      // Add services to invoice
      for (ServiceItem serviceItem : selectedServices) {
        ServiceInvoice serviceInvoice = ServiceInvoice.builder()
            .service(serviceItem.getService())
            .quantity(serviceItem.getQuantity())
            .amount(serviceItem.getSubtotal())
            .build();
        invoice.addService(serviceInvoice);
      }

      // Add products to invoice
      for (ProductItem productItem : selectedProducts) {
        InvoiceProduct invoiceProduct = InvoiceProduct.builder()
            .product(productItem.getProduct())
            .quantity(productItem.getQuantity())
            .price(productItem.getProduct().getSalesPrice())
            .amount(productItem.getSubtotal())
            .build();
        invoice.addProduct(invoiceProduct);
      }

      // Calculate totals
      invoice.calculateTotals();

      // Save invoice
      invoiceService.create(invoice);

      NotificationUtils.success("Factura generada automáticamente");

    } catch (Exception e) {
      NotificationUtils.error("Error al generar la factura: " + e.getMessage());
    }
  }

  public void openForNew() {
    clearForm();
    saveButton.setText("Registrar Consulta");
    editingConsultation = null;
    setHeaderTitle("Nueva Consulta");
    open();
  }

  public void openForEdit(Consultation consultation) {
    this.editingConsultation = consultation;
    binder.readBean(consultation);
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
  }

  private void setupServiceForm() {
    serviceForm.addServiceSavedListener(dto -> {
      // Refresh service selector and auto-select the new service
      loadComboBoxData();
      // Find and select the newly created service
      serviceService.getAllActiveServices().stream()
          .filter(s -> s.getName().equals(dto.getName()))
          .findFirst();
      serviceComboBox.setItems(serviceService.getAllActiveServices());
      serviceService.getAllActiveServices().stream()
          .filter(s -> s.getName().equals(dto.getName()))
              .findFirst()
          .ifPresent(serviceComboBox::setValue);
    });
  }

  // Inner classes for grid items
  public static class ServiceItem {
    private final Service service;
    private final Double quantity;
    private final BigDecimal subtotal;

    public ServiceItem(Service service, Double quantity) {
      this.service = service;
      this.quantity = quantity;
      this.subtotal = service.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Service getService() {
      return service;
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