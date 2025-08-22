package com.wornux.views.transactions;

import static com.wornux.utils.CSSUtility.CARD_BACKGROUND_COLOR;
import static com.wornux.utils.CSSUtility.SLIDER_RESPONSIVE_WIDTH;
import static com.wornux.utils.CommonUtils.comboBoxItemFilter;
import static com.wornux.utils.CommonUtils.createIconItem;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.HasClearButton;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.components.DecimalField;
import com.wornux.components.Sidebar;
import com.wornux.data.entity.*;
import com.wornux.mapper.ClientMapper;
import com.wornux.services.AuditService;
import com.wornux.services.implementations.InvoiceService;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.ProductService;
import com.wornux.services.interfaces.ServiceService;
import com.wornux.services.report.InvoiceReportService;
import com.wornux.utils.CommonUtils;
import com.wornux.utils.MenuBarHandler;
import com.wornux.utils.NotificationUtils;
import com.wornux.utils.logs.RevisionView;
import com.wornux.views.customers.ClientCreationDialog;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@Slf4j
public class InvoiceForm extends Div {

  private final ComboBox<Client> customer = new ComboBox<>("Selecciona un cliente");
  private final TextField docNum = new TextField("Número de factura");
  private final TextField salesOrder = new TextField("Número de orden/servicio");
  private final DatePicker issuedDate = new DatePicker("Fecha de emisión");
  private final DatePicker paymentDate = new DatePicker("Fecha de pago");

  private final DecimalField subtotalField = new DecimalField("Subtotal");
  private final DecimalField taxField = new DecimalField("Impuesto");
  private final DecimalField total = new DecimalField("Total");
  private final TextArea notes = new TextArea("Notas");

  private final Grid<Object> gridItems = new Grid<>();
  private final List<InvoiceProduct> invoiceProducts = new ArrayList<>();
  private final List<Object> displayedItems = new ArrayList<>();

  private final Binder<Invoice> binder = new BeanValidationBinder<>(Invoice.class);

  private final Sidebar sidebar = new Sidebar();
  private final Button add = new Button(VaadinIcon.PLUS_CIRCLE.create());
  private final Button exportPdfButton =
      new Button("Exportar PDF", VaadinIcon.FILE_TEXT_O.create());
  private final Div layoutTabBar = new Div();
  private final Div layoutGrid = new Div();
  private final Div createDetails = new Div();
  private final Div footer = new Div();
  private final transient InvoiceService invoiceService;
  private final transient ClientService customerService;
  private final transient InvoiceReportService invoiceReportService;
  private final transient RevisionView<Invoice> revisionView;
  private final ClientCreationDialog clientCreationDialog;
  private final transient List<Product> products;
  private Invoice element;
  @Setter private transient Runnable callable;

  public InvoiceForm(
      InvoiceService invoiceService,
      ClientService customerService,
      ProductService productService,
      ServiceService serviceService,
      AuditService auditService,
      ClientMapper clientMapper,
      InvoiceReportService invoiceReportService) {
    this.invoiceService = invoiceService;
    this.customerService = customerService;
    this.invoiceReportService = invoiceReportService;

    CommonUtils.commentsFormat(notes, 500);

    notes.setMinRows(4);
    notes.setMaxRows(4);

    this.products = productService.getAllProducts();

    this.revisionView = new RevisionView<>(auditService, Invoice.class);
    revisionView.configureGridRevision();

    clientCreationDialog = new ClientCreationDialog(customerService, clientMapper);

    docNum.setEnabled(false);
    subtotalField.setEnabled(false);
    taxField.setEnabled(false);
    total.setEnabled(false);

    createGrid();

    binder.bindInstanceFields(this);

    binder
        .forField(customer)
        .asRequired("Debes seleccionar un cliente")
        .bind(Invoice::getClient, Invoice::setClient);
    binder
        .getFields()
        .forEach(
            field -> {
              if (field instanceof HasClearButton clear) {
                clear.setClearButtonVisible(true);
              }
            });

    layoutGrid.addClassNames(CARD_BACKGROUND_COLOR, LumoUtility.Padding.SMALL);

    layoutTabBar.addClassNames(CARD_BACKGROUND_COLOR, LumoUtility.Padding.SMALL);
    layoutTabBar.addClassNames(
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.Width.AUTO,
        LumoUtility.Height.FULL);

    footer.add(notes, subtotalField, taxField, total);
    footer.addClassNames(
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.ROW,
        LumoUtility.Padding.SMALL,
        LumoUtility.JustifyContent.BETWEEN,
        LumoUtility.Background.CONTRAST_5);
    footer.getStyle().set("border-bottom-left-radius", "var(--lumo-space-m)");
    footer.getStyle().set("border-bottom-right-radius", "var(--lumo-space-m)");

    createDetails.add(createDetails());

    sidebar.createHeaderContent(createTabBar());
    sidebar.createContent(layoutTabBar, createDetails, layoutGrid, footer);
    sidebar.addClassNames(SLIDER_RESPONSIVE_WIDTH);
    sidebar.addSubTitle("Completa el formulario para crear una factura.");

    sidebar.setOnSaveClickListener(this::saveOrUpdate);
    sidebar.setOnCancelClickListener(this::cancel);

    sidebar.getSave().setText("Guardar y continuar");

    add(sidebar, clientCreationDialog);

    refreshGrid();

    Runnable updateHelperText =
        () -> {
          LocalDate issued = issuedDate.getValue();
          LocalDate payment = paymentDate.getValue();

          if (issued != null && payment != null) {
            long daysBetween = ChronoUnit.DAYS.between(issued, payment);
            paymentDate.setHelperText("Dentro de " + daysBetween + " días");
          } else {
            paymentDate.setHelperText("Selecciona fechas válidas");
          }
        };

    issuedDate.addValueChangeListener(e -> updateHelperText.run());
    paymentDate.addValueChangeListener(e -> updateHelperText.run());

    updateHelperText.run();
  }

  private static Div headerLayout(Component... components) {
    Div layoutForm = new Div(components);
    layoutForm.addClassNames(
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.ROW,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.Gap.Column.MEDIUM,
        LumoUtility.AlignItems.START,
        LumoUtility.JustifyContent.BETWEEN,
        LumoUtility.Padding.NONE,
        LumoUtility.Margin.Top.SMALL);
    return layoutForm;
  }

  private void createGrid() {
    gridItems.setItems(displayedItems);

    gridItems.setWidthFull();
    gridItems.setHeight("300px");
    gridItems.setEmptyStateText("No se encontraron registros.");
    gridItems.setMultiSort(true, Grid.MultiSortPriority.APPEND);
    gridItems.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    gridItems.addClassNames(LumoUtility.Margin.Top.XSMALL, LumoUtility.Margin.Bottom.XSMALL);

    gridItems
        .addColumn(new ComponentRenderer<>(this::renderActions))
        .setHeader("Acciones")
        .setFlexGrow(0)
        .setTextAlign(ColumnTextAlign.CENTER);

    gridItems
        .addColumn(
            item -> {
              if (item instanceof InvoiceProduct) {
                return Optional.ofNullable(((InvoiceProduct) item).getProduct())
                    .map(Product::getName)
                    .orElse("");
              } else if (item instanceof ServiceInvoice) {
                return Optional.ofNullable(((ServiceInvoice) item).getService())
                    .map(com.wornux.data.entity.Service::getName)
                    .orElse("");
              }
              return "";
            })
        .setHeader("Productos y Servicios")
        .setAutoWidth(true);

    gridItems
        .addColumn(
            item -> {
              if (item instanceof InvoiceProduct) {
                return Optional.ofNullable(((InvoiceProduct) item).getProduct())
                    .map(Product::getDescription)
                    .orElse("");
              } else if (item instanceof ServiceInvoice) {
                return Optional.ofNullable(((ServiceInvoice) item).getService())
                    .map(com.wornux.data.entity.Service::getDescription)
                    .orElse("");
              }
              return "";
            })
        .setHeader("Descripción")
        .setAutoWidth(true);

    gridItems
        .addColumn(
            item -> {
              Double quantity = 0.0;
              if (item instanceof InvoiceProduct) {
                quantity = ((InvoiceProduct) item).getQuantity();
              } else if (item instanceof ServiceInvoice) {
                quantity = ((ServiceInvoice) item).getQuantity();
              }
              return new DecimalFormat("#,##0.00")
                  .format(Optional.ofNullable(quantity).orElse(0.0));
            })
        .setHeader("Cantidad")
        .setAutoWidth(true)
        .setTextAlign(ColumnTextAlign.CENTER);

    gridItems
        .addColumn(
            item -> {
              BigDecimal price = BigDecimal.ZERO;
              if (item instanceof InvoiceProduct) {
                price = ((InvoiceProduct) item).getPrice();
              } else if (item instanceof ServiceInvoice) {
                price = ((ServiceInvoice) item).getPrice();
              }
              return new DecimalFormat("#,##0.00")
                  .format(Optional.ofNullable(price).orElse(BigDecimal.ZERO));
            })
        .setHeader("Precio")
        .setAutoWidth(true)
        .setTextAlign(ColumnTextAlign.END);

    gridItems
        .addColumn(
            item -> {
              BigDecimal amount = BigDecimal.ZERO;
              if (item instanceof InvoiceProduct) {
                amount = ((InvoiceProduct) item).getAmount();
              } else if (item instanceof ServiceInvoice) {
                amount = ((ServiceInvoice) item).getAmount();
              }
              return new DecimalFormat("#,##0.00")
                  .format(Optional.ofNullable(amount).orElse(BigDecimal.ZERO));
            })
        .setHeader("Importe")
        .setAutoWidth(true)
        .setTextAlign(ColumnTextAlign.END);

    gridItems.getDataProvider().addDataProviderListener(event -> calculateTotals());
    gridItems.addItemDoubleClickListener(
        event -> {
          Object item = event.getItem();
          if (item instanceof InvoiceProduct) {
            createProductDialog((InvoiceProduct) item);
          }
        });
  }

  private static final BigDecimal TAX_RATE = new BigDecimal("0.18"); // Assuming 18% tax rate

  private void calculateTotals() {
    BigDecimal productsTotal =
        invoiceProducts.stream()
            .filter(p -> p.getProduct() != null && p.getAmount() != null)
            .map(InvoiceProduct::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal servicesTotal =
        Optional.ofNullable(element)
            .map(Invoice::getServices)
            .map(List::stream)
            .orElse(Stream.empty())
            .filter(s -> s.getService() != null && s.getAmount() != null)
            .map(ServiceInvoice::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal currentSubtotal = productsTotal.add(servicesTotal);
    BigDecimal currentTax = currentSubtotal.multiply(TAX_RATE);
    BigDecimal currentTotal = currentSubtotal.add(currentTax);

    subtotalField.setValue(currentSubtotal.doubleValue());
    taxField.setValue(currentTax.doubleValue());
    total.setValue(currentTotal.doubleValue());
  }

  private Component renderActions(Object item) {
    if (item instanceof InvoiceProduct) {
      boolean isNew = ((InvoiceProduct) item).getProduct() == null;
      Button actionButton;
      if (isNew) {
        actionButton = new Button(VaadinIcon.PLUS_CIRCLE_O.create());
        actionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        actionButton.addClickListener(e -> createProductDialog((InvoiceProduct) item));
        return actionButton;
      } else {
        actionButton = new Button(LumoIcon.EDIT.create());
        actionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        actionButton.addClickListener(e -> createProductDialog((InvoiceProduct) item));

        Button removeButton = new Button(VaadinIcon.MINUS_CIRCLE_O.create());
        removeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        removeButton.addClickListener(
            e -> {
              invoiceProducts.remove(item);
              refreshGrid();
            });

        Div actions = new Div(actionButton, removeButton);
        actions.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, "-mx-s");
        return actions;
      }
    }
    return new Div(); // No actions for services
  }

  private void createProductDialog(InvoiceProduct line) {
    boolean isNew = line.getProduct() == null;
    Dialog d = new Dialog();

    final NumberField fieldQty = new NumberField("Cantidad");
    fieldQty.setMin(0.1);
    fieldQty.setStep(0.1);
    fieldQty.setWidthFull();
    fieldQty.setValue(Optional.ofNullable(line.getQuantity()).orElse(1.0));
    fieldQty.setStepButtonsVisible(true);
    fieldQty.setClearButtonVisible(true);

    final DecimalField fieldPrice = new DecimalField("Precio");
    fieldPrice.setWidthFull();
    fieldPrice.setValue(Optional.ofNullable(line.getPrice()).orElse(BigDecimal.ZERO).doubleValue());
    fieldPrice.setClearButtonVisible(true);

    final ComboBox<Product> fieldProduct = createProductComboBox(fieldQty, fieldPrice);
    if (line.getProduct() != null) {
      fieldProduct.setValue(line.getProduct());
    }

    final Button apply = new Button("Aplicar", VaadinIcon.CHECK_CIRCLE.create());
    apply.addClassNames(LumoUtility.Width.AUTO);

    BeanValidationBinder<InvoiceProduct> binderLine =
        new BeanValidationBinder<>(InvoiceProduct.class);

    binderLine.setBean(line);

    binderLine.forField(fieldProduct).asRequired("Este campo no puede estar vacío").bind("product");

    binderLine
        .forField(fieldPrice)
        .asRequired("Este campo no puede estar vacío")
        .bind(
            invoiceProduct ->
                Optional.ofNullable(invoiceProduct.getPrice())
                    .orElse(BigDecimal.ZERO)
                    .doubleValue(),
            (invoiceProduct, aDouble) -> invoiceProduct.setPrice(BigDecimal.valueOf(aDouble)));

    binderLine
        .forField(fieldQty)
        .asRequired("Este campo no puede estar vacío")
        .bind(
            invoiceProduct -> Optional.ofNullable(invoiceProduct.getQuantity()).orElse(1.0),
            InvoiceProduct::setQuantity);

    apply.addThemeVariants(
        ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
    apply.addClickListener(
        event -> {
          try {
            binderLine.writeBean(line);
            if (element == null) {
              element = new Invoice();
            }
            line.setInvoice(element);
            line.calculateAmount();
            if (isNew) {
              invoiceProducts.remove(line); // Remove empty one
              invoiceProducts.add(line); // Add filled one
              invoiceProducts.add(new InvoiceProduct()); // Add new empty one
            }
            refreshGrid();
            d.close();
          } catch (ValidationException validationException) {
            NotificationUtils.error(validationException);
          }
        });

    VerticalLayout dialogLayout = new VerticalLayout(fieldProduct, fieldQty, fieldPrice);
    dialogLayout.setAlignItems(FlexComponent.Alignment.START);
    dialogLayout.setPadding(false);
    dialogLayout.getStyle().set("min-width", "500px").set("height", "250px");

    d.setHeaderTitle("Productos y Servicios");
    d.getFooter().add(apply);

    d.setCloseOnOutsideClick(true);
    d.setDraggable(true);
    d.setOpened(true);
    d.add(dialogLayout);
  }

  private ComboBox<Product> createProductComboBox(NumberField fieldQty, DecimalField fieldPrice) {
    final ComboBox<Product> fieldProduct = new ComboBox<>("Producto");
    fieldProduct.setItemLabelGenerator(Product::getName);
    fieldProduct.setClearButtonVisible(true);
    fieldProduct.setWidthFull();

    List<Product> copy = new ArrayList<>(products);

    copy.removeIf(
        product ->
            invoiceProducts.stream()
                .map(InvoiceProduct::getProduct)
                .filter(Objects::nonNull)
                .toList()
                .contains(product));
    fieldProduct.setItems(copy);
    fieldProduct.addValueChangeListener(
        event -> {
          if (event.getValue() != null) {
            fieldQty.setValue(1.0);
            fieldPrice.setValue(
                Optional.ofNullable(event.getValue().getSalesPrice())
                    .orElse(BigDecimal.ZERO)
                    .doubleValue());
          }
        });
    return fieldProduct;
  }

  public void close() {
    sidebar.close();
  }

  public void open() {
    populateForm(null);
    sidebar.newObject("Nueva Factura");
  }

  public void edit(Invoice element) {
    populateForm(element);
    sidebar.editObject("Editar Factura");
  }

  private void cancel(ClickEvent<Button> buttonClickEvent) {
    sidebar.close();
  }

  private void saveOrUpdate(ClickEvent<Button> buttonClickEvent) {
    try {
      // 1. Initialize Invoice element
      if (element == null) {
        element = new Invoice();
      }

      // 2. Populate basic fields from binder
      binder.writeBean(this.element);

      // Set calculated subtotal, tax, and total on the Invoice entity
      this.element.setSubtotal(BigDecimal.valueOf(subtotalField.getValue()));
      this.element.setTax(BigDecimal.valueOf(taxField.getValue()));
      this.element.setTotal(BigDecimal.valueOf(total.getValue()));

      // 3. Validate and manage Client
      if (element.getClient() == null) {
        NotificationUtils.error("Debes seleccionar un cliente");
        return;
      }
      // Fetch a managed client instance if not already managed
      if (element.getClient().getId() != null) {
        Client managedClient =
            customerService.getClientById(element.getClient().getId()).orElse(null);
        if (managedClient == null) {
          NotificationUtils.error("El cliente seleccionado no se encontró en la base de datos.");
          return;
        }
        element.setClient(managedClient);
      } else {
        NotificationUtils.error(
            "El cliente no ha sido guardado correctamente. Intenta crearlo de nuevo.");
        return;
      }

      boolean isNewInvoice = (element.getCode() == null || element.getCode() == 0);

      if (isNewInvoice) {
        element =
            invoiceService.create(
                element); // This saves the Invoice and returns the managed instance
      }

      element.getProducts().clear();
      element.getServices().clear();

      Set<InvoiceProduct> finalProducts =
          invoiceProducts.stream().filter(p -> p.getProduct() != null).collect(Collectors.toSet());
      finalProducts.forEach(
          element::addProduct); // addProduct sets the back-reference to the managed 'element'

      List<ServiceInvoice> servicesFromDisplayedItems =
          displayedItems.stream()
              .filter(item -> item instanceof ServiceInvoice)
              .map(item -> (ServiceInvoice) item)
              .collect(Collectors.toList());
      servicesFromDisplayedItems.forEach(
          element::addService); // addService sets the back-reference to the managed 'element'

      if (element.getProducts().isEmpty() && element.getServices().isEmpty()) {
        NotificationUtils.error("Debes seleccionar al menos un producto o servicio");
        return;
      }

      invoiceService.create(element);

      populateForm(element);
      Optional.ofNullable(callable).ifPresent(Runnable::run);

    } catch (ObjectOptimisticLockingFailureException ex) {
      log.error(ex.getLocalizedMessage());
      NotificationUtils.error(
          "Error al actualizar los datos. Alguien más ha actualizado el registro mientras realizabas cambios.");
    } catch (ValidationException ex) {
      log.error(ex.getLocalizedMessage());
      NotificationUtils.error(ex);
    } catch (Exception ex) {
      log.error("Error al guardar la factura: {}", ex.getMessage(), ex);
      NotificationUtils.error("Error al guardar la factura: " + ex.getMessage());
    }
  }

  private Div createTabBar() {
    MenuBar menuBar = new MenuBar();
    menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

    MenuItem general =
        createIconItem(menuBar, VaadinIcon.INFO_CIRCLE.create(), "Información general");
    MenuItem jobHistorial =
        createIconItem(menuBar, VaadinIcon.TIME_BACKWARD.create(), "Registro de actividad");

    MenuBarHandler menuBarHandler = new MenuBarHandler(menuBar, layoutTabBar);
    menuBarHandler.addMenuItem(general, generalForm());
    menuBarHandler.addMenuItem(jobHistorial, jobLogForm());

    menuBarHandler.setDefaultMenuItem(general);
    menuBarHandler.addMenuItemSelectionListener(
        event -> {
          layoutGrid.setVisible(event == general);
          createDetails.setVisible(event == general);
          footer.setVisible(event == general);

          if (event == general) {
            layoutTabBar.getStyle().remove("border-bottom-left-radius");
            layoutTabBar.getStyle().remove("border-bottom-right-radius");
          } else {
            layoutTabBar.getStyle().set("border-bottom-left-radius", "var(--lumo-space-m)");
            layoutTabBar.getStyle().set("border-bottom-right-radius", "var(--lumo-space-m)");
          }
        });

    exportPdfButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
    exportPdfButton.addClickListener(e -> exportToPdf());
    exportPdfButton.setEnabled(element != null && element.getCode() != null);

    Div tabs = getDiv(menuBar);
    tabs.getStyle().set("border-top-left-radius", "var(--lumo-space-m)");
    tabs.getStyle().set("border-top-right-radius", "var(--lumo-space-m)");

    return tabs;
  }

  private @NotNull Div getDiv(MenuBar menuBar) {
    Div menuBarContainer = new Div(menuBar);
    menuBarContainer.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Flex.GROW);

    Div buttonContainer = new Div(exportPdfButton);
    buttonContainer.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER);

    Div tabsContent = new Div(menuBarContainer, buttonContainer);
    tabsContent.addClassNames(
        LumoUtility.Display.FLEX,
        LumoUtility.JustifyContent.BETWEEN,
        LumoUtility.AlignItems.CENTER,
        LumoUtility.Width.FULL);

    Div tabs = new Div(tabsContent);
    tabs.addClassNames(
        LumoUtility.Padding.SMALL,
        LumoUtility.Gap.MEDIUM,
        LumoUtility.Background.CONTRAST_10,
        LumoUtility.Margin.Horizontal.MEDIUM,
        LumoUtility.Margin.Top.MEDIUM);
    return tabs;
  }

  private void populateForm(Invoice value) {
    this.element = value;

    binder.readBean(element);
    customer.focus();

    invoiceProducts.clear();

    if (element == null) {
      docNum.setValue(invoiceService.getNextInvoiceNumber());
      issuedDate.setValue(LocalDate.now());
      paymentDate.setValue(LocalDate.now().plusDays(30));
      customer.setEnabled(true);
      add.setVisible(true);
      exportPdfButton.setEnabled(false);
      invoiceProducts.add(new InvoiceProduct());
    } else {
      docNum.setValue(String.valueOf(element.getCode()));
      issuedDate.setValue(element.getIssuedDate());
      paymentDate.setValue(element.getPaymentDate());

      revisionView.loadRevisions(element.getCode());

      invoiceProducts.addAll(element.getProducts());

      customer.setEnabled(false);
      add.setVisible(false);
      exportPdfButton.setEnabled(true);
    }

    refreshGrid();
  }

  private void refreshGrid() {
    displayedItems.clear();
    displayedItems.addAll(invoiceProducts);
    if (element != null && element.getServices() != null) {
      displayedItems.addAll(element.getServices());
    }
    gridItems.getDataProvider().refreshAll();
    calculateTotals();
  }

  private void populateInvoiceLinesFromCustomer(Client customer) {
    if (element == null) {
      element = new Invoice();
    }
  }

  private Div createDetails() {
    MenuBar menuBar = new MenuBar();
    menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);
    createIconItem(menuBar, VaadinIcon.PAPERCLIP.create(), "Productos y Servicios");

    Div header = new Div(menuBar);
    header.addClassNames(
        LumoUtility.Padding.SMALL,
        LumoUtility.Gap.MEDIUM,
        LumoUtility.Background.CONTRAST_10,
        LumoUtility.BorderRadius.MEDIUM);

    Div container = new Div(header, gridItems);
    container.addClassNames(LumoUtility.BorderRadius.MEDIUM, CARD_BACKGROUND_COLOR);

    return container;
  }

  private Div generalForm() {
    Div layout = new Div(headerLayout(createLeftHeaderForm(), createRightHeaderForm()), layoutGrid);
    layout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

    return layout;
  }

  private Div createLeftHeaderForm() {

    add.setTooltipText("Agregar un cliente");
    add.addThemeVariants(
        ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
    add.addClassNames(LumoUtility.Width.AUTO);
    add.getStyle().setCursor("pointer");

    Span bill = new Span("Facturar a");
    Span contactName = new Span();
    Span contactEmail = new Span();
    Span address = new Span();
    Span phone = new Span();
    Span email = new Span();
    Hr line = new Hr();

    bill.addClassNames(
        LumoUtility.FontWeight.MEDIUM,
        LumoUtility.Margin.Top.SMALL,
        LumoUtility.Margin.Horizontal.XSMALL);
    bill.setVisible(false);
    line.setVisible(false);

    Arrays.asList(contactName, contactEmail, address, phone, email)
        .forEach(
            c -> {
              c.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
              c.addClassNames(LumoUtility.Margin.Horizontal.XSMALL);
            });

    List<Client> allCustomerByDisabledIsFalse = customerService.getAllActiveClients();
    customer.setClearButtonVisible(true);
    customer.setItems(
        comboBoxItemFilter(Client::getFirstName, String::contains), allCustomerByDisabledIsFalse);
    customer.setItemLabelGenerator(Client::getFirstName);
    customer.setRenderer(
        new ComponentRenderer<>(
            item -> {
              Div container = new Div();
              container.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

              Span title = new Span(item.getFirstName());
              title.addClassNames(LumoUtility.FontWeight.BOLD);

              Span subtitle = new Span(item.getFullName());
              subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

              container.add(title, subtitle);
              return container;
            }));
    customer.addValueChangeListener(
        event -> {
          bill.setVisible(event.getValue() != null);
          line.setVisible(event.getValue() != null);
          if (event.getValue() != null) {
            contactName.setText(event.getValue().getFullName());
            contactEmail.setText(event.getValue().getEmail());
            address.setText(event.getValue().getStreetAddress());
            phone.setText(event.getValue().getPhoneNumber());
            email.setText(event.getValue().getEmail());
          } else {
            Arrays.asList(contactName, contactEmail, address, phone, email)
                .forEach(c -> c.setText(""));
          }

          populateInvoiceLinesFromCustomer(event.getValue());
        });
    add.addClickListener(
        event -> {
          clientCreationDialog.setOnClientCreated(
              item -> {
                customer.setValue(item); // Set the value first
                customer.getDataProvider().refreshAll(); // Then refresh
              });
          clientCreationDialog.openDialog();
        });

    Div header = new Div(customer, add);
    customer.setWidthFull();
    header.addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW, LumoUtility.AlignItems.END);

    Div form = new Div(header, bill, contactName, contactEmail, address, line, phone, email);
    form.setWidthFull();
    form.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
    form.setMaxWidth(500, Unit.PIXELS);

    return form;
  }

  private Div createRightHeaderForm() {
    DatePicker.DatePickerI18n dateFormat = new DatePicker.DatePickerI18n();
    dateFormat.setFirstDayOfWeek(1);

    Arrays.asList(issuedDate, paymentDate)
        .forEach(
            c -> {
              c.setClearButtonVisible(true);
              c.setWeekNumbersVisible(true);
              c.setI18n(dateFormat);
            });

    Div form = new Div(docNum, salesOrder, issuedDate, paymentDate);
    form.setWidthFull();
    form.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
    form.setMaxWidth(400, Unit.PIXELS);

    return form;
  }

  private Div jobLogForm() {
    H1 headerTitle = new H1("Registro de actividad");

    Paragraph description =
        new Paragraph(
            "Registro cronológico de todas las acciones realizadas en esta factura. Incluye cambios de estado, interacciones de usuario, notas y cualquier modificación para trazabilidad completa.");
    description.addClassNames(
        LumoUtility.Display.HIDDEN, LumoUtility.Display.Breakpoint.Large.FLEX);

    Div headerLayout = new Div(headerTitle, description, revisionView.getGrid());
    headerLayout.addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Margin.Top.SMALL);

    return headerLayout;
  }

  private void exportToPdf() {
    if (element == null || element.getCode() == null) {
      NotificationUtils.error("No se puede exportar una factura que no ha sido guardada.");
      return;
    }

    try {
      var fileName = "Invoice_" + element.getCode();

      var data = invoiceReportService.generateInvoicePdf(element);

      InvoiceView.exportInvoiceInPdfFormat(fileName, data);

      NotificationUtils.success("PDF generado exitosamente.");

    } catch (Exception e) {
      log.error("Error al generar el PDF de la factura: {}", element.getCode(), e);
      NotificationUtils.error(
          "Error al generar el PDF, favor intentar nuevamente en unos minutos.");
    }
  }
}
