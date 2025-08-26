package com.wornux.views.grooming;

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
import com.wornux.data.entity.WaitingRoom;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.InvoiceStatus;
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

/**
 * Form para registrar sesiones de Grooming (estética), siguiendo el patrón de ConsultationsForm
 * (ítems en memoria -> Invoice).
 */
public class GroomingForm extends Dialog {

  // Fields
  // private final ComboBox<Pet> petComboBox = new ComboBox<>("Mascota");

  // NUEVO

  // Mascota (selector por diálogo)
  private final TextField petName = new TextField("Mascota");
  private final Button selectPetButton = new Button("Seleccionar");
  private Pet selectedPet;
  private final SelectPetDialog selectPetDialog;

  private final ComboBox<Employee> groomerComboBox = new ComboBox<>("Groomer");
  private final TextArea notesTextArea = new TextArea("Notas de Grooming");

  // Servicios (estética)
  private final ComboBox<Offering> serviceComboBox = new ComboBox<>("Seleccionar Servicio");
  private final Button addServiceButton = new Button("Agregar", new Icon(VaadinIcon.PLUS));
  private final Grid<OfferingItem> servicesGrid = new Grid<>(OfferingItem.class, false);

  // Productos
  private final ComboBox<Product> productComboBox = new ComboBox<>("Seleccionar Producto");
  private final NumberField productQuantityField = new NumberField("Cantidad");
  private final Button addProductButton = new Button("Agregar", new Icon(VaadinIcon.PLUS));
  private final Grid<ProductItem> productsGrid = new Grid<>(ProductItem.class, false);

  // Totales (UI)
  private final Span totalServicesSpan = new Span("$0.00");
  private final Span totalProductsSpan = new Span("$0.00");
  private final Span grandTotalSpan = new Span("$0.00");

  // Acciones
  private final Button saveButton = new Button("Registrar");
  private final Button cancelButton = new Button("Cancelar");
  private final Button createServiceButton = new Button("Crear");
  private final OfferingForm offeringForm;

  // Binder
  private final Binder<GroomingSession> binder = new Binder<>(GroomingSession.class);

  // Services
  private final transient GroomingSessionService groomingSessionService;
  private final transient EmployeeService employeeService;
  private final transient PetService petService;
  private final transient OfferingService offeringService;
  private final transient ProductService productService;
  private final transient InvoiceService invoiceService;

  // Estado
  private transient GroomingSession editingSession;
  private final List<OfferingItem> selectedServices = new ArrayList<>();
  private final List<ProductItem> selectedProducts = new ArrayList<>();

  private WaitingRoom sourceWaitingRoom;

  @Setter private transient Consumer<GroomingSession> onSaveCallback;

  public GroomingForm(
      GroomingSessionService groomingSessionService,
      EmployeeService employeeService,
      PetService petService,
      OfferingService offeringService,
      InvoiceService invoiceService,
      ProductService productService) {
    this.groomingSessionService = groomingSessionService;
    this.employeeService = employeeService;
    this.petService = petService;
    this.offeringService = offeringService;
    this.invoiceService = invoiceService;
    this.productService = productService;
    this.offeringForm = new OfferingForm(offeringService);
    // NUEVO
    this.selectPetDialog = new SelectPetDialog(petService);

    petName.setReadOnly(true);
    selectPetButton.addClickListener(e -> selectPetDialog.open());
    selectPetDialog.addPetSelectedListener(
        pet -> {
          selectedPet = pet;
          petName.setInvalid(false);
          petName.setValue(pet != null ? pet.getName() : "");
        });

    setHeaderTitle("Sesión de Grooming");
    setModal(true);
    setWidth("1200px");
    setHeight("800px");

    createForm();
    setupValidation();
    setupEventListeners();
    loadComboBoxData();
    setupOfferingForm();
  }

  private void createForm() {
    // Básicos
    // petComboBox.setItemLabelGenerator(p -> p.getName() + " (" + p.getType() + ")");
    // petComboBox.setRequired(true);
    // petComboBox.setWidthFull();
    HorizontalLayout petPickerLayout = new HorizontalLayout(petName, selectPetButton);
    petPickerLayout.setAlignItems(FlexComponent.Alignment.END);
    petName.setWidthFull();

    groomerComboBox.setItemLabelGenerator(emp -> emp.getFirstName() + " " + emp.getLastName());
    groomerComboBox.setRequired(true);
    groomerComboBox.setWidthFull();

    notesTextArea.setRequired(true);
    notesTextArea.setHeight("100px");
    notesTextArea.setWidthFull();

    // Servicios de estética
    serviceComboBox.setItemLabelGenerator(s -> s.getName() + " - $" + s.getPrice());
    serviceComboBox.setWidthFull();
    serviceComboBox.setPlaceholder("Buscar servicios de estética...");

    addServiceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

    // Productos internos
    productComboBox.setItemLabelGenerator(p -> p.getName() + " - $" + p.getSalesPrice());
    productComboBox.setWidthFull();
    productComboBox.setPlaceholder("Buscar productos de uso interno...");

    productQuantityField.setValue(1.0);
    productQuantityField.setMin(0.1);
    productQuantityField.setStep(0.1);
    productQuantityField.setWidth("120px");

    addProductButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

    // Layout
    FormLayout basicInfoLayout = new FormLayout();
    // basicInfoLayout.add(petComboBox, groomerComboBox);
    basicInfoLayout.add(petPickerLayout, groomerComboBox);
    basicInfoLayout.add(notesTextArea, 2);

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

    H4 title = new H4("Servicios de Estética");

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
        .addColumn(item -> item.getOffering().getName())
        .setHeader("Servicio")
        .setAutoWidth(true);
    servicesGrid
        .addColumn(item -> "$" + item.getOffering().getPrice())
        .setHeader("Precio")
        .setAutoWidth(true);
    servicesGrid.addColumn(OfferingItem::getQuantity).setHeader("Cantidad").setAutoWidth(true);
    servicesGrid
        .addColumn(item -> "$" + item.getSubtotal())
        .setHeader("Subtotal")
        .setAutoWidth(true);

    servicesGrid
        .addComponentColumn(this::createOfferingRemoveButton)
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

  private Button createOfferingRemoveButton(OfferingItem item) {
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
    /*binder.forField(petComboBox)
    .withValidator(p -> p != null, "Debe seleccionar una mascota")
    .bind(GroomingSession::getPet, GroomingSession::setPet);*/

    binder
        .forField(groomerComboBox)
        .withValidator(g -> g != null, "Debe seleccionar un groomer")
        .bind(GroomingSession::getGroomer, GroomingSession::setGroomer);

    binder
        .forField(notesTextArea)
        .withValidator(
            notes -> notes != null && !notes.trim().isEmpty(), "Las notas son requeridas")
        .bind(GroomingSession::getNotes, GroomingSession::setNotes);
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

    boolean alreadyAdded =
        selectedServices.stream()
            .anyMatch(item -> item.getOffering().getId().equals(selectedOffering.getId()));

    if (alreadyAdded) {
      NotificationUtils.error("Este servicio ya ha sido agregado");
      return;
    }

    OfferingItem offeringItem = new OfferingItem(selectedOffering, 1.0);
    selectedServices.add(offeringItem);
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
            .map(OfferingItem::getSubtotal)
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

  /*private void loadComboBoxData() {
      // Mascotas
      petService.getAllPets().forEach(pet -> petComboBox.getListDataView().addItem(pet));

      // Groomers
      employeeService.getGroomers()
              .forEach(emp -> groomerComboBox.getListDataView().addItem(emp));

      // Servicios de estética
      serviceService.findGroomingServices()
              .forEach(service -> serviceComboBox.getListDataView().addItem(service));

      // Productos internos
      productService.findInternalUseProducts()
              .forEach(product -> productComboBox.getListDataView().addItem(product));
  }*/

  /*private void loadComboBoxData() {
    // Mascotas
    // petComboBox.setItems(petService.getAllPets());

    // Groomers
    groomerComboBox.setItems(employeeService.getGroomers());

    // Servicios de estética
    serviceComboBox.setItems(serviceService.findGroomingServices());

    // Productos internos
    productComboBox.setItems(productService.findInternalUseProducts());
  }*/

  private void loadComboBoxData() {
    // Groomer (con comportamiento por rol)
    applyDefaultGroomerSelection();

    // Servicios de estética
    if (offeringService != null) {
      serviceComboBox.setItems(offeringService.findGroomingServices());
    }

    // Productos internos
    if (productService != null) {
      productComboBox.setItems(productService.findInternalUseProducts());
    }
  }

  private void save(ClickEvent<Button> event) {
    try {
      if (editingSession == null) {
        editingSession = new GroomingSession();
        editingSession.setGroomingDate(LocalDateTime.now());
      }

      if (selectedPet == null) {
        petName.setInvalid(true);
        petName.setErrorMessage("Debe seleccionar una mascota");
        NotificationUtils.error("Debe seleccionar una mascota");
        return;
      }

      binder.writeBean(editingSession);
      editingSession.setPet(selectedPet);
      if (groomerComboBox.getValue() != null) { // por si el binder no lo capturó aún
        editingSession.setGroomer(groomerComboBox.getValue());
      }

      GroomingSession saved = groomingSessionService.save(editingSession);

      if (!selectedServices.isEmpty() || !selectedProducts.isEmpty()) {
        recreateInvoiceFor(saved);
      }

      // Si viene de waiting room, finalizar sesión y cerrar WR (liberar groomer)
      if (sourceWaitingRoom != null) { // <<<
        try {
          // Método espejo al de consulta: debe existir en tu GroomingSessionServiceImpl
          groomingSessionService.finish(saved.getId()); // <<<
        } catch (Exception ex) {
          NotificationUtils.error(
              "La sesión se guardó, pero no se pudo cerrar la sala de espera: " + ex.getMessage());
        }
      }

      NotificationUtils.success("Grooming registrado y finalizado."); // mensaje espejo
      if (onSaveCallback != null) onSaveCallback.accept(saved);
      close();

    } catch (ValidationException e) {
      NotificationUtils.error("Por favor, complete todos los campos requeridos");
    } catch (Exception e) {
      NotificationUtils.error("Error al guardar: " + e.getMessage());
    }
  }

  /*private void save(ClickEvent<Button> event) {
    try {
      if (editingSession == null) {
        editingSession = new GroomingSession();
        editingSession.setGroomingDate(LocalDateTime.now());
      }

      if (selectedPet == null) {
        petName.setInvalid(true);
        petName.setErrorMessage("Debe seleccionar una mascota");
        NotificationUtils.error("Debe seleccionar una mascota");
        return;
      }

      binder.writeBean(editingSession);
      editingSession.setPet(selectedPet);

      // Guarda o actualiza la sesión de grooming
      GroomingSession saved = groomingSessionService.save(editingSession);

      // Si hay ítems, recrea la factura (borra la pendiente y crea nueva)
      if (!selectedServices.isEmpty() || !selectedProducts.isEmpty()) {
        recreateInvoiceFor(saved);
      }

      NotificationUtils.success("Grooming registrado exitosamente");

      if (onSaveCallback != null) {
        onSaveCallback.accept(saved);
      }
      close();

    } catch (ValidationException e) {
      NotificationUtils.error("Por favor, complete todos los campos requeridos");
    } catch (Exception e) {
      NotificationUtils.error("Error al guardar: " + e.getMessage());
    }
  }*/

  private void recreateInvoiceFor(GroomingSession session) {
    try {
      // 1) Buscar factura del mismo grooming
      Invoice existing = invoiceService.findByGroomingId(session.getId()).orElse(null);

      // 2) Si existe y está pagada, no tocar
      if (existing != null && existing.getStatus() == InvoiceStatus.PAID) {
        NotificationUtils.info("Factura asociada pagada. No se realizaron cambios en la factura.");
        return;
      }

      // 3) Si existe y NO está pagada, eliminarla
      if (existing != null) {
        try {
          invoiceService.delete(existing.getCode());
          NotificationUtils.info("Factura anterior eliminada para crear una nueva.");
        } catch (Exception e) {
          NotificationUtils.error("Error al eliminar la factura asociada pendiente anterior.");
          return;
        }
      }

      // 4) Construir nueva factura (ahora con vínculo grooming)
      Invoice invoice =
          Invoice.builder()
              .client(session.getPet().getOwners().iterator().next())
              .grooming(session)
              .issuedDate(LocalDate.now())
              .paymentDate(LocalDate.now().plusDays(30))
              .status(InvoiceStatus.PENDING)
              .paidToDate(BigDecimal.ZERO)
              .groomingNotes("Grooming para " + session.getPet().getName())
              .subtotal(BigDecimal.ZERO)
              .tax(BigDecimal.ZERO)
              .total(BigDecimal.ZERO)
              .build();

      // 5) Agregar líneas
      for (OfferingItem si : selectedServices) {
        InvoiceOffering line =
            InvoiceOffering.builder()
                .offering(si.getOffering())
                .quantity(si.getQuantity())
                .amount(si.getSubtotal())
                .build();
        invoice.addOffering(line);
      }

      for (ProductItem pi : selectedProducts) {
        InvoiceProduct line =
            InvoiceProduct.builder()
                .product(pi.getProduct())
                .quantity(pi.getQuantity())
                .price(pi.getProduct().getSalesPrice())
                .amount(pi.getSubtotal())
                .build();
        invoice.addProduct(line);
      }

      // 6) Totales y persistir
      invoice.calculateTotals();
      invoiceService.create(invoice);
      NotificationUtils.success("Factura generada automáticamente");
    } catch (Exception e) {
      NotificationUtils.error("Error al recrear la factura: " + e.getMessage());
    }
  }

  /*private void save(ClickEvent<Button> event) {
    try {
      if (editingSession == null) {
        editingSession = new GroomingSession();
        editingSession.setGroomingDate(LocalDateTime.now());
      }

      if (selectedPet == null) {
        petName.setInvalid(true);
        petName.setErrorMessage("Debe seleccionar una mascota");
        NotificationUtils.error("Debe seleccionar una mascota");
        return;
      }

      binder.writeBean(editingSession);

      editingSession.setPet(selectedPet);

      // Derivar cliente desde la mascota si lo necesitas en tu flujo de facturación
      // (Si GroomingSession no tiene client, la factura tomará el owner del pet)
      GroomingSession saved = groomingSessionService.save(editingSession);

      if (!selectedServices.isEmpty() || !selectedProducts.isEmpty()) {
        createAutomaticInvoice(saved);
      }

      NotificationUtils.success("Grooming registrado exitosamente");

      if (onSaveCallback != null) {
        onSaveCallback.accept(saved);
      }
      close();

    } catch (ValidationException e) {
      NotificationUtils.error("Por favor, complete todos los campos requeridos");
    } catch (Exception e) {
      NotificationUtils.error("Error al guardar: " + e.getMessage());
    }
  }*/

  private void createAutomaticInvoice(GroomingSession session) {
    try {
      Invoice invoice =
          Invoice.builder()
              .client(session.getPet().getOwners().iterator().next())
              .issuedDate(LocalDate.now())
              .paymentDate(LocalDate.now().plusDays(30))
              .status(InvoiceStatus.PENDING)
              .notes("Grooming para " + session.getPet().getName())
              .subtotal(BigDecimal.ZERO)
              .tax(BigDecimal.ZERO)
              .total(BigDecimal.ZERO)
              .paidToDate(BigDecimal.ZERO)
              .build();

      for (OfferingItem offeringItem : selectedServices) {
        InvoiceOffering offeringInvoice =
            InvoiceOffering.builder()
                .offering(offeringItem.getOffering())
                .quantity(offeringItem.getQuantity())
                .amount(offeringItem.getSubtotal())
                .build();
        invoice.addOffering(offeringInvoice);
      }

      for (ProductItem productItem : selectedProducts) {
        InvoiceProduct invoiceProduct =
            InvoiceProduct.builder()
                .product(productItem.getProduct())
                .quantity(productItem.getQuantity())
                .price(productItem.getProduct().getSalesPrice())
                .amount(productItem.getSubtotal())
                .build();
        invoice.addProduct(invoiceProduct);
      }

      invoice.calculateTotals();
      invoiceService.create(invoice);

      NotificationUtils.success("Factura generada automáticamente");

    } catch (Exception e) {
      NotificationUtils.error("Error al generar la factura: " + e.getMessage());
    }
  }

  public void openForNew() {
    clearForm();

    selectedPet = null;
    petName.clear();
    petName.setInvalid(false);

    applyDefaultGroomerSelection();

    saveButton.setText("Registrar");
    editingSession = null;
    setHeaderTitle("Nuevo Grooming");
    open();
  }

  /*public void openForEdit(GroomingSession session) {
    this.editingSession = session;
    binder.readBean(session);
    selectedPet = session.getPet();
    petName.setValue(selectedPet != null ? selectedPet.getName() : "");
    petName.setInvalid(false);
    saveButton.setText("Actualizar");
    setHeaderTitle("Editar Grooming");
    open();
  }*/

  public void openForEdit(GroomingSession session) {
    this.editingSession = session;
    binder.readBean(session);
    selectedPet = session.getPet();
    petName.setValue(selectedPet != null ? selectedPet.getName() : "");
    petName.setInvalid(false);
    saveButton.setText("Actualizar");
    setHeaderTitle("Editar Grooming");

    // Cargar líneas desde factura existente (con fetch de detalle)
    invoiceService
        .findByGroomingIdWithDetails(session.getId())
        .ifPresent(
            inv -> {
              selectedServices.clear();
              inv.getOfferings()
                  .forEach(
                      si ->
                          selectedServices.add(
                              new OfferingItem(si.getOffering(), si.getQuantity())));
              servicesGrid.setItems(selectedServices);

              selectedProducts.clear();
              inv.getProducts()
                  .forEach(
                      ip ->
                          selectedProducts.add(new ProductItem(ip.getProduct(), ip.getQuantity())));
              productsGrid.setItems(selectedProducts);

              updateTotals();
            });

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
  }

  /*private void setupServiceForm() {
      serviceForm.addServiceSavedListener(dto -> {
          // Refrescar selector y auto-seleccionar el nuevo servicio (filtrado a grooming)
          serviceComboBox.getListDataView().removeItems(serviceComboBox.getListDataView().getItems().toList());
          serviceService.findGroomingServices()
                  .forEach(service -> serviceComboBox.getListDataView().addItem(service));

          serviceService.getAllActiveServices().stream()
                  .filter(s -> s.getName().equals(dto.getName()))
                  .findFirst()
                  .ifPresent(serviceComboBox::setValue);
      });
  }
  */

  private void setupOfferingForm() {
    offeringForm.addServiceSavedListener(
        dto -> {
          // Refrescar los servicios de GROOMING en el combo
          var groomingOfferings = offeringService.findGroomingServices();
          serviceComboBox.setItems(groomingOfferings); // <-- primero poblar

          // Seleccionar el recién creado (por nombre). Mejor si luego pasas el ID.
          groomingOfferings.stream()
              .filter(s -> s.getName().equalsIgnoreCase(dto.getName()))
              .findFirst()
              .ifPresent(serviceComboBox::setValue); // <-- después seleccionar
        });
  }

  // Clases de grid
  public static class OfferingItem {
    private final Offering offering;
    private final Double quantity;
    private final BigDecimal subtotal;

    public OfferingItem(Offering offering, Double quantity) {
      this.offering = offering;
      this.quantity = quantity;
      this.subtotal = offering.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Offering getOffering() {
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

  public void presetForGroomerAndPet(Pet pet, Employee groomer, boolean lockGroomerField) {
    // Mascota
    this.selectedPet = pet;
    this.petName.setValue(pet != null ? pet.getName() : "");
    this.petName.setInvalid(false);
    this.petName.setReadOnly(true);
    this.selectPetButton.setEnabled(false);

    // Groomer
    if (groomer != null) {
      java.util.List<Employee> items = new java.util.ArrayList<>();
      this.groomerComboBox
          .getDataProvider()
          .fetch(new com.vaadin.flow.data.provider.Query<>())
          .forEach(items::add);
      boolean present = items.stream().anyMatch(e -> e.getId().equals(groomer.getId()));
      if (!present) {
        items.add(groomer);
        this.groomerComboBox.setItems(items);
      }
      this.groomerComboBox.setValue(groomer);
      if (lockGroomerField) {
        this.groomerComboBox.setReadOnly(true);
      }
    }
  }

  public void attachWaitingRoom(WaitingRoom wr) {
    this.sourceWaitingRoom = wr;
  }

  private void applyDefaultGroomerSelection() {
    if (UserUtils.hasEmployeeRole(EmployeeRole.GROOMER)) {
      Employee currentGroomer = UserUtils.getCurrentEmployee().orElse(null);
      if (currentGroomer != null) {
        groomerComboBox.setItems(java.util.List.of(currentGroomer));
        groomerComboBox.setValue(currentGroomer);
        groomerComboBox.setReadOnly(true);
      }
    } else if (employeeService != null) {
      groomerComboBox.setReadOnly(false);
      groomerComboBox.setItems(employeeService.getGroomers());
    }
  }
}
