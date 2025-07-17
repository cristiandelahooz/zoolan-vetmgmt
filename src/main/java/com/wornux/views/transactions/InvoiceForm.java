package com.wornux.views.transactions;

import com.wornux.components.*;
import com.wornux.data.entity.*;
import com.wornux.services.*;
import com.wornux.services.implementations.InvoiceService;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.ProductService;
import com.wornux.utils.CommonUtils;
import com.wornux.utils.MenuBarHandler;
import com.wornux.utils.NotificationUtils;
import com.wornux.utils.logs.RevisionView;
import com.wornux.views.customers.ClientForm;
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
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.wornux.utils.CSSUtility.CARD_BACKGROUND_COLOR;
import static com.wornux.utils.CSSUtility.SLIDER_RESPONSIVE_WIDTH;
import static com.wornux.utils.CommonUtils.comboBoxItemFilter;
import static com.wornux.utils.CommonUtils.createIconItem;

@Slf4j
public class InvoiceForm extends Div {

    private final ComboBox<Client> customer = new ComboBox<>("Choose a customer");
    private final TextField docNum = new TextField("Invoice number");
    private final TextField salesOrder = new TextField("P.O./S.O. number");
    private final DatePicker issuedDate = new DatePicker("Invoice date");
    private final DatePicker paymentDate = new DatePicker("Payment date");

    private final DecimalField total = new DecimalField("Total");
    private final TextArea notes = new TextArea("Notes");

    private final Grid<InvoiceProduct> gridProductService = new Grid<>(InvoiceProduct.class, false);
    private final Set<InvoiceProduct> invoiceProducts = new HashSet<>();
    private final Map<String, InputStream> fileAttachments = new HashMap<>();
    private final Binder<Invoice> binder = new BeanValidationBinder<>(Invoice.class);
    private final Sidebar sidebar = new Sidebar();
    private final Div layoutTabBar = new Div();
    private final Div layoutGrid = new Div();
    private final Div createDetails = new Div();
    private final Div footer = new Div();
    private final InvoiceService service;
    private final ClientService customerService;
    private final RevisionView<Invoice> revisionView;
    private final ClientForm customerForm;
    private final ProductServiceForm productServiceForm;
    private final List<Product> products;
    private Invoice element;
    @Setter
    private Runnable callable;

    public InvoiceForm(InvoiceService service, ClientService customerService, ProductService productService,
            AuditService auditService) {
        this.service = service;
        this.customerService = customerService;

        CommonUtils.commentsFormat(notes, 10000);

        notes.setMinRows(4);
        notes.setMaxRows(4);

        this.products = productService.getAllProducts();

        this.revisionView = new RevisionView<>(auditService, Invoice.class);
        revisionView.configureGridRevision();

        customerForm = new ClientForm(customerService);
        productServiceForm = new ProductServiceForm(productService);
        productServiceForm.setConsumer(products::add);

        docNum.setEnabled(false);
        total.setEnabled(false);

        createGrid();
        createUpload();

        binder.bindInstanceFields(this);
        binder.getFields().forEach(field -> {
            if (field instanceof HasClearButton clear) {
                clear.setClearButtonVisible(true);
            }
        });

        layoutGrid.addClassNames(CARD_BACKGROUND_COLOR, LumoUtility.Padding.SMALL);

        layoutTabBar.addClassNames(CARD_BACKGROUND_COLOR, LumoUtility.Padding.SMALL);
        layoutTabBar.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Width.AUTO,
                LumoUtility.Height.FULL);

        footer.add(notes, total);
        footer.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW, LumoUtility.Padding.SMALL,
                LumoUtility.JustifyContent.BETWEEN, LumoUtility.Background.CONTRAST_5);
        footer.getStyle().set("border-bottom-left-radius", "var(--lumo-space-m)");
        footer.getStyle().set("border-bottom-right-radius", "var(--lumo-space-m)");

        sidebar.createContent(layoutTabBar, createDetails, layoutGrid, footer);
        sidebar.addClassNames(SLIDER_RESPONSIVE_WIDTH);
        sidebar.addSubTitle("And fill out the form below to create an invoice.");

        sidebar.setOnSaveClickListener(this::saveOrUpdate);
        sidebar.setOnCancelClickListener(this::cancel);

        sidebar.getSave().setText("Save and continue");

        add(sidebar, customerForm, productServiceForm);

        invoiceProducts.add(new InvoiceProduct());
        gridProductService.getDataProvider().refreshAll();

        Runnable updateHelperText = () -> {
            LocalDate issued = issuedDate.getValue();
            LocalDate payment = paymentDate.getValue();

            if (issued != null && payment != null) {
                long daysBetween = ChronoUnit.DAYS.between(issued, payment);
                paymentDate.setHelperText("Within " + daysBetween + " days");
            } else {
                paymentDate.setHelperText("Select valid dates");
            }
        };

        issuedDate.addValueChangeListener(e -> updateHelperText.run());
        paymentDate.addValueChangeListener(e -> updateHelperText.run());

        updateHelperText.run();

    }

    private static Div headerLayout(Component... components) {
        Div layoutForm = new Div(components);
        layoutForm.addClassNames(
                // < 1024 pixels
                LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW,
                // > 1024 pixels
                LumoUtility.FlexDirection.COLUMN,
                // Horizontal spacing between components
                LumoUtility.Gap.Column.MEDIUM, LumoUtility.AlignItems.START, LumoUtility.JustifyContent.BETWEEN,
                LumoUtility.Padding.NONE, LumoUtility.Margin.Top.SMALL);
        return layoutForm;
    }

    private void createGrid() {

        gridProductService.setItems(invoiceProducts);

        gridProductService.addColumn(new ComponentRenderer<>(this::renderActions)).setHeader("Actions").setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER);
        gridProductService.addColumn(c -> Optional.ofNullable(c.getProduct()).map(Product::getName).orElse(""))
                .setHeader("Products & services").setAutoWidth(true);
        gridProductService.addColumn(c -> Optional.ofNullable(c.getProduct()).map(Product::getDescription).orElse(""))
                .setHeader("Description").setAutoWidth(true);
        gridProductService.addColumn(
                        c -> new DecimalFormat("#,##0.00").format(Optional.ofNullable(c.getQuantity()).orElse(0.0)))
                .setHeader("Quantity").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);
        gridProductService.addColumn(
                        c -> new DecimalFormat("#,##0.00").format(Optional.ofNullable(c.getPrice()).orElse(BigDecimal.ZERO)))
                .setHeader("Price").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        gridProductService.addColumn(
                        c -> new DecimalFormat("#,##0.00").format(Optional.ofNullable(c.getAmount()).orElse(BigDecimal.ZERO)))
                .setHeader("Amount").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        gridProductService.getDataProvider().addDataProviderListener(event -> calculateTotal());
        gridProductService.addItemDoubleClickListener(event -> {
            createDialog(event.getItem());
        });

    }

    private void calculateTotal() {
        double lineTotal = 0;
        if (element == null || element.getCode() == null) {
            lineTotal += invoiceProducts.stream().filter(p -> p.getProduct() != null)
                    .mapToDouble(m -> m.getAmount().doubleValue()).sum();
        } else {
            lineTotal += element.getProducts().stream().mapToDouble(m -> m.getAmount().doubleValue()).sum();
        }

        total.setValue(lineTotal);
    }

    private Component renderActions(InvoiceProduct item) {

        Button add = new Button(item.getProduct() == null ? VaadinIcon.PLUS_CIRCLE_O.create() : LumoIcon.EDIT.create());
        add.addClassNames(LumoUtility.Width.AUTO, LumoUtility.Margin.NONE);
        add.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        add.getStyle().set("cursor", "pointer");
        add.addClickListener(e -> {
            createDialog(item);
        });

        if (item.getProduct() == null) {
            return add;
        }

        Button edit = new Button(VaadinIcon.MINUS_CIRCLE_O.create());
        edit.addClassNames(LumoUtility.Width.AUTO, LumoUtility.Margin.NONE);
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        edit.getStyle().set("cursor", "pointer");
        edit.addClickListener(e -> {
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

        final NumberField fieldQty = new NumberField("Quantity");
        fieldQty.setMin(0.1);
        fieldQty.setStep(0.1);
        fieldQty.setWidthFull();
        fieldQty.setValue(Optional.ofNullable(line.getQuantity()).orElse(1.0));
        fieldQty.setStepButtonsVisible(true);
        fieldQty.setClearButtonVisible(true);

        final DecimalField fieldPrice = new DecimalField("Price");
        fieldPrice.setWidthFull();
        fieldPrice.setValue(Optional.ofNullable(line.getPrice()).orElse(BigDecimal.ZERO).doubleValue());
        fieldPrice.setClearButtonVisible(true);

        final ComboBox<Product> fieldProduct = getProductComboBox(fieldQty, fieldPrice);

        final Button apply = new Button("Apply", VaadinIcon.CHECK_CIRCLE.create());
        final Button add = new Button("Create new product", VaadinIcon.PLUS_CIRCLE.create());
        apply.addClassNames(LumoUtility.Width.AUTO);
        add.addClassNames(LumoUtility.Width.AUTO);

        BeanValidationBinder<InvoiceProduct> binderLine = new BeanValidationBinder<>(InvoiceProduct.class);

        binderLine.setBean(line);

        binderLine.forField(fieldProduct).asRequired("This field cannot be null").bind("product");

        binderLine.forField(fieldPrice).asRequired("This field cannot be null")
                .bind(invoiceProduct -> Optional.ofNullable(invoiceProduct.getPrice()).orElse(BigDecimal.ZERO)
                                .doubleValue(),
                        (invoiceProduct, aDouble) -> invoiceProduct.setPrice(BigDecimal.valueOf(aDouble)));

        binderLine.forField(fieldQty).asRequired("This field cannot be null")
                .bind(invoiceProduct -> Optional.ofNullable(invoiceProduct.getQuantity()).orElse(1.0),
                        InvoiceProduct::setQuantity);

        add.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        add.addClickListener(event -> {
            productServiceForm.open();
        });

        apply.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        apply.addClickListener(event -> {

            try {

                binderLine.writeBean(line);

                if (element == null) {
                    element = new Invoice();
                }

                line.setInvoice(element);

                invoiceProducts.stream().filter(p -> p.equals(line)).findFirst().ifPresent(obj -> {
                    obj.setAmount(line.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
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

        d.setHeaderTitle("Products & Services");
        d.getFooter().add(add, apply);

        d.setCloseOnOutsideClick(true);
        d.setDraggable(true);
        d.setOpened(true);
        d.add(dialogLayout);
    }

    private ComboBox<Product> getProductComboBox(NumberField fieldQty, DecimalField fieldPrice) {
        final ComboBox<Product> fieldProduct = new ComboBox<>("Product");
        fieldProduct.setItemLabelGenerator(Product::getName);
        fieldProduct.setClearButtonVisible(true);
        fieldProduct.setWidthFull();

        List<Product> copy = new ArrayList<>(products);

        copy.removeIf(product -> invoiceProducts.stream().map(InvoiceProduct::getProduct).toList().contains(product));
        fieldProduct.setItems(copy);
        fieldProduct.addValueChangeListener(event -> {
            fieldQty.setValue(1.0);
            fieldPrice.setValue(Optional.ofNullable(event.getValue().getPrice()).orElse(BigDecimal.ZERO).doubleValue());
        });
        return fieldProduct;
    }

    public void close() {
        sidebar.close();
    }

    public void open() {
        populateForm(null);
        sidebar.newObject("New Invoice");
    }

    public void edit(Invoice element) {
        populateForm(element);
        sidebar.editObject("Edit Invoice");
    }

    private void cancel(ClickEvent<Button> buttonClickEvent) {
        sidebar.close();
    }

    private void saveOrUpdate(ClickEvent<Button> buttonClickEvent) {
        try {

            if (element == null) {
                element = new Invoice();
            }

            element.setSubtotal(BigDecimal.valueOf(total.getValue()));
            element.setTax(BigDecimal.ZERO);

            Set<InvoiceProduct> tmpInvoiceProducts = gridProductService.getGenericDataView().getItems()
                    .filter(p -> p.getProduct() != null).collect(Collectors.toSet());

            AtomicBoolean error = new AtomicBoolean(false);

            if (error.get()) {
                binder.writeBean(this.element);
                return;
            }
            binder.writeBean(this.element);

            ConfirmationDialog.confirmation(event -> {

                element.setProducts(tmpInvoiceProducts);
                service.create(element);

                //                fileAttachments.forEach(
                //                        (key, value) -> storageService.save(value, key, StorageRecordType.INVOICE, element.getCode()));

                populateForm(element);
                Optional.ofNullable(callable).ifPresent(Runnable::run);
            });

        } catch (ObjectOptimisticLockingFailureException ex) {
            log.error(ex.getLocalizedMessage());
            NotificationUtils.error(
                    "Error updating the data. Somebody else has updated the record while you were making changes.");
        } catch (ValidationException ex) {
            log.error(ex.getLocalizedMessage());
            NotificationUtils.error(ex);
        }
    }

    private void populateForm(Invoice value) {
        this.element = value;

        binder.readBean(element);
        customer.focus();

        docNum.clear();

        issuedDate.setValue(LocalDate.now());
        paymentDate.setValue(LocalDate.now().plusDays(30));

        invoiceProducts.clear();
        invoiceProducts.add(new InvoiceProduct());

        customer.setEnabled(element == null);

        if (element != null) {
            docNum.setValue(String.valueOf(element.getCode()));
            issuedDate.setValue(element.getIssuedDate());
            paymentDate.setValue(element.getPaymentDate());

            revisionView.loadRevisions(element.getCode());

            invoiceProducts.addAll(element.getProducts());
        }
        gridProductService.getDataProvider().refreshAll();
    }

    private void populateInvoiceLinesFromCustomer(Client customer) {

        if (element == null) {
            element = new Invoice();
        }
    }

    private Div generalForm() {
        Div layout = new Div(headerLayout(createLeftHeaderForm(), createRightHeaderForm()), layoutGrid);
        layout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        return layout;
    }

    private Div createLeftHeaderForm() {

        Button add = new Button(VaadinIcon.PLUS_CIRCLE.create());
        add.setTooltipText("Add a customer");

        Button edit = new Button(LumoIcon.EDIT.create());
        edit.setVisible(false);

        Arrays.asList(add, edit).forEach(c -> {
            c.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            c.addClassNames(LumoUtility.Width.AUTO);
            c.getStyle().setCursor("pointer");
        });

        Span bill = new Span("Bill to");
        Span contactName = new Span();
        Span contactEmail = new Span();
        Span address = new Span();
        Span phone = new Span();
        Span email = new Span();
        Hr line = new Hr();

        bill.addClassNames(LumoUtility.FontWeight.MEDIUM, LumoUtility.Margin.Top.SMALL,
                LumoUtility.Margin.Horizontal.XSMALL);
        bill.setVisible(false);
        line.setVisible(false);

        Arrays.asList(contactName, contactEmail, address, phone, email).forEach(c -> {
            c.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
            c.addClassNames(LumoUtility.Margin.Horizontal.XSMALL);
        });

        List<Client> allCustomerByDisabledIsFalse = customerService.getAllActiveClients();
        customer.setClearButtonVisible(true);
        customer.setItems(comboBoxItemFilter(Client::getFirstName, String::contains), allCustomerByDisabledIsFalse);
        customer.setItemLabelGenerator(Client::getFirstName);
        customer.setRenderer(new ComponentRenderer<>(item -> {
            Div container = new Div();
            container.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

            Span title = new Span(item.getFirstName());
            title.addClassNames(LumoUtility.FontWeight.BOLD);

            Span subtitle = new Span(item.getEmergencyContactName());
            subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

            container.add(title, subtitle);
            return container;
        }));
        customer.addValueChangeListener(event -> {
            bill.setVisible(event.getValue() != null);
            line.setVisible(event.getValue() != null);
            edit.setVisible(event.getValue() != null);
            if (event.getValue() != null) {
                contactName.setText(event.getValue().getEmergencyContactName());
                contactEmail.setText(event.getValue().getEmail());
                address.setText(event.getValue().getStreetAddress());
                phone.setText(event.getValue().getPhoneNumber());
                email.setText(event.getValue().getEmail());
                edit.setTooltipText("Edit (%s)".formatted(event.getValue().getFirstName()));
            } else {
                Arrays.asList(contactName, contactEmail, address, phone, email).forEach(c -> c.setText(""));
            }

            populateInvoiceLinesFromCustomer(event.getValue());
        });

        add.addClickListener(event -> {
            customerForm.open();
            customerForm.setConsumer(item -> {
                customer.getDataProvider().refreshAll();
                customer.setValue(item);
            });
        });

        edit.addClickListener(event -> {
            customerForm.open(customer.getValue());
            customerForm.setConsumer(item -> {
                if (item.isActive()) {
                    customer.getDataProvider().refreshAll();
                    customer.setValue(item);
                } else {
                    customer.setItems(comboBoxItemFilter(Client::getFirstName, String::contains),
                            customerService.getAllActiveClients());
                }
            });
        });

        Div header = new Div(customer, add, edit);
        customer.setWidthFull();
        header.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW, LumoUtility.AlignItems.END);

        Div form = new Div(header, bill, contactName, contactEmail, address, line, phone, email);
        form.setWidthFull();
        form.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
        form.setMaxWidth(500, Unit.PIXELS);

        return form;
    }

    private Div createRightHeaderForm() {
        DatePicker.DatePickerI18n dateFormat = new DatePicker.DatePickerI18n();
        dateFormat.setFirstDayOfWeek(1);

        Arrays.asList(issuedDate, paymentDate).forEach(c -> {
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
        H1 headerTitle = new H1("Activity log");

        Paragraph description = new Paragraph(
                "Chronological record of all actions performed on this work order. Includes status changes, user interactions, notes, and any modifications for full traceability.");
        description.addClassNames(LumoUtility.Display.HIDDEN, LumoUtility.Display.Breakpoint.Large.FLEX);

        Div headerLayout = new Div(headerTitle, description, revisionView.getGrid());
        headerLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Margin.Top.SMALL);

        return headerLayout;
    }

    private Div attachedForm() {
        H1 headerTitle = new H1("Attachments");

        Paragraph description = new Paragraph(
                "Complete list of all files in this property. Search, organize and manage the content stored here.");
        description.addClassNames(LumoUtility.Display.HIDDEN, LumoUtility.Display.Breakpoint.Large.FLEX);

        Div headerLayout = new Div(headerTitle, description);
        headerLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Margin.Top.SMALL);

        return headerLayout;
    }

    private void createUpload() {
        Button button = new Button("Click to upload");
        button.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        SvgIcon svgIcon = LineAwesomeIcon.UPLOAD_SOLID.create();
        svgIcon.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderRadius.LARGE, LumoUtility.Height.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL, LumoUtility.Margin.Horizontal.AUTO, "order-first",
                LumoUtility.Padding.SMALL, LumoUtility.Width.MEDIUM);

        Span label = new Span("or drag and drop");
        label.addClassNames("-ms-s");

        Icon icon = LumoIcon.UPLOAD.create();
        icon.addClassNames(LumoUtility.Display.HIDDEN);

    }

}
