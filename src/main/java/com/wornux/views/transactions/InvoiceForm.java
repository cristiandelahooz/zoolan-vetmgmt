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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
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
import com.wornux.components.ConfirmationDialog;
import com.wornux.components.DecimalField;
import com.wornux.components.Sidebar;
import com.wornux.data.entity.Client;
import com.wornux.data.entity.Invoice;
import com.wornux.data.entity.InvoiceProduct;
import com.wornux.data.entity.Product;
import com.wornux.mapper.ClientMapper;
import com.wornux.services.AuditService;
import com.wornux.services.implementations.InvoiceService;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.ProductService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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

  private final DecimalField total = new DecimalField("Total");
  private final TextArea notes = new TextArea("Notas");

  private final Grid<InvoiceProduct> gridProductService = new Grid<>(InvoiceProduct.class, false);
  private final Set<InvoiceProduct> invoiceProducts = new HashSet<>();

  private final Binder<Invoice> binder = new BeanValidationBinder<>(Invoice.class);

  private final Sidebar sidebar = new Sidebar();
  private final Button add = new Button(VaadinIcon.PLUS_CIRCLE.create());
  private final Button exportPdfButton =
      new Button("Exportar PDF", VaadinIcon.FILE_TEXT_O.create());
  private final Div layoutTabBar = new Div();
  private final Div layoutGrid = new Div();
  private final Div createDetails = new Div();
  private final Div footer = new Div();
  private final InvoiceService service;
  private final ClientService customerService;
  private final InvoiceReportService invoiceReportService;
  private final RevisionView<Invoice> revisionView;
  private final ClientCreationDialog clientCreationDialog;
  private final List<Product> products;
  private Invoice element;
  @Setter
  private Runnable callable;

  public InvoiceForm(
      InvoiceService service,
      ClientService customerService,
      ProductService productService,
      AuditService auditService,
      ClientMapper clientMapper,
      InvoiceReportService invoiceReportService) {
    this.service = service;
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
    total.setEnabled(false);

    createGrid();

    binder.bindInstanceFields(this);

    // Manual binding for customer field since it's not an instance field of Invoice
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

    footer.add(notes, total);
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

    invoiceProducts.add(new InvoiceProduct());
    gridProductService.getDataProvider().refreshAll();

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
    gridProductService.setItems(invoiceProducts);

    List.of(gridProductService)
        .forEach(
            c -> {
              c.setWidthFull();
              c.setHeight("300px");
              c.setEmptyStateText("No se encontraron registros.");
              c.setMultiSort(true, Grid.MultiSortPriority.APPEND);
              c.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
              c.addClassNames(LumoUtility.Margin.Top.XSMALL, LumoUtility.Margin.Bottom.XSMALL);
            });

    gridProductService
        .addColumn(new ComponentRenderer<>(this::renderActions))
        .setHeader("Acciones")
        .setFlexGrow(0)
        .setTextAlign(ColumnTextAlign.CENTER);
    gridProductService
        .addColumn(c -> Optional.ofNullable(c.getProduct()).map(Product::getName).orElse(""))
        .setHeader("Productos y servicios")
        .setAutoWidth(true);
    gridProductService
        .addColumn(c -> Optional.ofNullable(c.getProduct()).map(Product::getDescription).orElse(""))
        .setHeader("Descripción")
        .setAutoWidth(true);
    gridProductService
        .addColumn(
            c ->
                new DecimalFormat("#,##0.00")
                    .format(Optional.ofNullable(c.getQuantity()).orElse(0.0)))
        .setHeader("Cantidad")
        .setAutoWidth(true)
        .setTextAlign(ColumnTextAlign.CENTER);
    gridProductService
        .addColumn(
            c ->
                new DecimalFormat("#,##0.00")
                    .format(Optional.ofNullable(c.getPrice()).orElse(BigDecimal.ZERO)))
        .setHeader("Precio")
        .setAutoWidth(true)
        .setTextAlign(ColumnTextAlign.END);
    gridProductService
        .addColumn(
            c ->
                new DecimalFormat("#,##0.00")
                    .format(Optional.ofNullable(c.getAmount()).orElse(BigDecimal.ZERO)))
        .setHeader("Importe")
        .setAutoWidth(true)
        .setTextAlign(ColumnTextAlign.END);
    gridProductService.getDataProvider().addDataProviderListener(event -> calculateTotal());
    gridProductService.addItemDoubleClickListener(event -> createDialog(event.getItem()));
  }

  private void calculateTotal() {
    double lineTotal = 0;
    if (element == null || element.getCode() == null) {
      lineTotal +=
          invoiceProducts.stream()
              .filter(p -> p.getProduct() != null)
              .mapToDouble(m -> m.getAmount().doubleValue())
              .sum();
    } else {
      lineTotal +=
          element.getProducts().stream().mapToDouble(m -> m.getAmount().doubleValue()).sum();
    }

    total.setValue(lineTotal);
  }

  private Component renderActions(InvoiceProduct item) {

    Button add =
        new Button(
            item.getProduct() == null ? VaadinIcon.PLUS_CIRCLE_O.create() : LumoIcon.EDIT.create());
    add.addClassNames(LumoUtility.Width.AUTO, LumoUtility.Margin.NONE);
    add.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    add.getStyle().set("cursor", "pointer");
    add.addClickListener(e -> createDialog(item));

    if (item.getProduct() == null) {
      return add;
    }

    Button edit = new Button(VaadinIcon.MINUS_CIRCLE_O.create());
    edit.addClassNames(LumoUtility.Width.AUTO, LumoUtility.Margin.NONE);
    edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    edit.getStyle().set("cursor", "pointer");
    edit.addClickListener(
        e -> {
          invoiceProducts.remove(item);
          gridProductService.getDataProvider().refreshAll();
        });

    Div actions = new Div(add, edit);
    actions.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER);
    actions.addClassNames("-mx-s");
    return actions;
  }

  private void createDialog(InvoiceProduct line) {
    var isNew = line.getProduct() == null;
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

    final ComboBox<Product> fieldProduct = getProductComboBox(fieldQty, fieldPrice);

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

            invoiceProducts.stream()
                .filter(p -> p.equals(line))
                .findFirst()
                .ifPresent(
                    obj -> {
                      obj.setAmount(
                          line.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
                    });

            if (isNew) {
              invoiceProducts.add(new InvoiceProduct());
            }
            gridProductService.getDataProvider().refreshAll();

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

  private ComboBox<Product> getProductComboBox(NumberField fieldQty, DecimalField fieldPrice) {
    final ComboBox<Product> fieldProduct = new ComboBox<>("Producto");
    fieldProduct.setItemLabelGenerator(Product::getName);
    fieldProduct.setClearButtonVisible(true);
    fieldProduct.setWidthFull();

    List<Product> copy = new ArrayList<>(products);

    copy.removeIf(
        product ->
            invoiceProducts.stream().map(InvoiceProduct::getProduct).toList().contains(product));
    fieldProduct.setItems(copy);
    fieldProduct.addValueChangeListener(
        event -> {
          fieldQty.setValue(1.0);
          fieldPrice.setValue(
              Optional.ofNullable(event.getValue().getSalesPrice())
                  .orElse(BigDecimal.ZERO)
                  .doubleValue());
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

      if (element == null) {
        element = new Invoice();
      }

      if (customer.getValue() == null) {
        NotificationUtils.error("Debes seleccionar un cliente");
        return;
      }
      element.setClient(customer.getValue());
      element.setNotes(notes.getValue());
      element.setSalesOrder(salesOrder.getValue());
      element.setSubtotal(BigDecimal.valueOf(total.getValue()));
      element.setTax(BigDecimal.ZERO);

      Set<InvoiceProduct> tmpInvoiceProducts =
          gridProductService
              .getGenericDataView()
              .getItems()
              .filter(p -> p.getProduct() != null)
              .collect(Collectors.toSet());

      if (tmpInvoiceProducts.isEmpty()) {
        NotificationUtils.error("Debes seleccionar al menos un producto o servicio");
        return;
      }

      binder.writeBean(this.element);

      boolean isNewInvoice = (element.getCode() == null || element.getCode() == 0);

      if (isNewInvoice) {
        element.setProducts(tmpInvoiceProducts);
        service.create(element);

        populateForm(element);
        Optional.ofNullable(callable).ifPresent(Runnable::run);
      } else {
        ConfirmationDialog.saveUpdate(
            event -> {
              element.setProducts(tmpInvoiceProducts);
              service.create(element);

              populateForm(element);
              Optional.ofNullable(callable).ifPresent(Runnable::run);
            });
      }

    } catch (ObjectOptimisticLockingFailureException ex) {
      log.error(ex.getLocalizedMessage());
      NotificationUtils.error(
          "Error al actualizar los datos. Alguien más ha actualizado el registro mientras realizabas cambios.");
    } catch (ValidationException ex) {
      log.error(ex.getLocalizedMessage());
      NotificationUtils.error(ex);
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

    // Configure PDF export button
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

    if (element == null) {
      // Show next invoice number for new invoices
      docNum.setValue(service.getNextInvoiceNumber());
      issuedDate.setValue(LocalDate.now());
      paymentDate.setValue(LocalDate.now().plusDays(30));

      invoiceProducts.clear();
      invoiceProducts.add(new InvoiceProduct());

      customer.setEnabled(true);
      // Show add button only for new invoices
      add.setVisible(true);
      // Disable export button for new invoices
      exportPdfButton.setEnabled(false);
    } else {
      // Show actual invoice number for existing invoices
      docNum.setValue(String.valueOf(element.getCode()));
      issuedDate.setValue(element.getIssuedDate());
      paymentDate.setValue(element.getPaymentDate());

      revisionView.loadRevisions(element.getCode());

      invoiceProducts.clear();
      invoiceProducts.addAll(element.getProducts());

      customer.setEnabled(false);
      // Hide add button for existing invoices - no client changes allowed
      add.setVisible(false);
      // Enable export button for existing invoices
      exportPdfButton.setEnabled(true);
    }

    gridProductService.getDataProvider().refreshAll();
  }

  private void populateInvoiceLinesFromCustomer(Client customer) {
    if (element == null) {
      element = new Invoice();
    }
  }

  private Div createDetails() {
    MenuBar menuBar = new MenuBar();
    menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

    MenuItem attached =
        createIconItem(menuBar, VaadinIcon.PAPERCLIP.create(), "Productos y Servicios");

    MenuBarHandler menuBarHandler = new MenuBarHandler(menuBar, layoutGrid);
    menuBarHandler.addMenuItem(attached, gridProductService);

    menuBarHandler.setDefaultMenuItem(attached);

    Div tabs = new Div(menuBar);
    tabs.addClassNames(
        LumoUtility.Padding.SMALL,
        LumoUtility.Gap.MEDIUM,
        LumoUtility.BorderRadius.MEDIUM,
        CARD_BACKGROUND_COLOR);

    return tabs;
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
                customer.getDataProvider().refreshAll();
                customer.setValue(item);
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
