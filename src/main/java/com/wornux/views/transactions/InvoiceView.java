package com.wornux.views.transactions;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Client;
import com.wornux.data.entity.Invoice;
import com.wornux.data.enums.InvoiceStatus;
import com.wornux.services.AuditService;
import com.wornux.services.implementations.InvoiceService;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.ProductService;
import com.wornux.services.report.pdf.JasperReportFactory;
import com.wornux.utils.GridUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;

import static com.wornux.utils.CommonUtils.comboBoxItemFilter;
import static com.wornux.utils.PredicateUtils.createPredicateForSelectedItems;
import static com.wornux.utils.PredicateUtils.predicateForNumericField;

import com.wornux.components.*;

@Slf4j
@Uses(Icon.class)
@Route(value = "invoices")
@PageTitle("Invoices Management")
@CssImport("./themes/zoolan-vetmgmt/view/invoice.css")
public class InvoiceView extends Div {

    private final Grid<Invoice> grid = GridUtils.createBasicGrid(Invoice.class);

    private final TextField docNum = new TextField("Número de factura#");
    private final MultiSelectComboBox<Client> customer = new MultiSelectComboBox<>("Todos los clientes");
    private final MultiSelectComboBox<InvoiceStatus> status = new MultiSelectComboBox<>("Todos los estados");
    private final DatePicker fromPeriod = new DatePicker("Desde");
    private final DatePicker toPeriod = new DatePicker("Hasta");
    private final Span quantity = new Span();
    private final JasperReportFactory reportFactory;

    private final BoardCards boardCards = new BoardCards();
    private final Button create = new Button();

    private final transient InvoiceService service;
    private final InvoiceForm invoiceForm;

    public InvoiceView(InvoiceService service, @Qualifier("clientServiceImpl") ClientService customerService,
            ProductService productService, AuditService auditService, JasperReportFactory reportFactory) {
        this.reportFactory = reportFactory;
        this.service = service;

        setId("invoices-view");

        invoiceForm = new InvoiceForm(service, customerService, productService, auditService);

        createGrid(service, createFilterSpecification());

        final Div gridLayout = new Div(grid);
        gridLayout.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Padding.SMALL,
                LumoUtility.Height.FULL);

        add(createTitle(), createBoardCards(), createFilter(), gridLayout, invoiceForm);
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
        setSizeFull();

        create.addClickListener(event -> invoiceForm.open());

        invoiceForm.setCallable(this::refreshAll);

        List<Client> allCustomerByDisabledIsFalse = customerService.getAllActiveClients();
        customer.setItems(comboBoxItemFilter(c -> c.getFirstName() + " " + c.getStreetAddress(), String::contains),
                allCustomerByDisabledIsFalse);

        recreateBoardCards();
    }

    private static Renderer<Invoice> renderCustomer() {
        return LitRenderer.<Invoice> of("""
                        <vaadin-horizontal-layout style="align-items: center;" theme="spacing">
                            <vaadin-avatar img="${item.pictureUrl}" name="${item.pictureUrl}"></vaadin-avatar>
                            <vaadin-vertical-layout style="line-height: var(--lumo-line-height-m);">
                                <span class="font-semibold">${item.name}</span>
                                <span class="text-s text-secondary">${item.address}</span>
                            </vaadin-vertical-layout>
                        </vaadin-horizontal-layout>
                        """).withProperty("pictureUrl", c -> c.getClient().getFirstName())
                .withProperty("name", c -> c.getClient().getFirstName())
                .withProperty("address", c -> c.getClient().getEmail());
    }

    private void createGrid(InvoiceService service, Specification<Invoice> specification) {

        GridUtils.configureGrid(grid, specification, service.getRepository());

        GridUtils.addComponentColumn(grid, this::renderStatus, "Estado", "status");

        GridUtils.addColumn(grid, Invoice::getIssuedDate, "Fecha", "issuedDate");

        GridUtils.addColumn(grid, Invoice::getCode, "Número", "code");

        GridUtils.addColumn(grid, renderCustomer(), "Cliente", "customer.name", "customer.email");

        GridUtils.addColumn(grid, c -> new DecimalFormat("#,##0.00").format(c.getTotal()), "Total", "total")
                .setTextAlign(ColumnTextAlign.END);

        GridUtils.addColumn(grid, c -> new DecimalFormat("#,##0.00").format(c.getTotal().subtract(c.getPaidToDate())),
                "Deuda total").setTextAlign(ColumnTextAlign.END);

        GridUtils.addComponentColumn(grid, this::renderActions, "Acciones").setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                invoiceForm.edit(event.getValue());
            } else {
                invoiceForm.close();
            }
        });
    }

    public Specification<Invoice> createFilterSpecification() {
        return (root, query, builder) -> {
            Order order = builder.desc(root.get("code"));
            //            assert query != null;
            //            query.orderBy(order);
            //            query.distinct(true);
            if (query != null) {
                query.orderBy(order);
            }

            Predicate predicateCode = predicateForNumericField(root, builder, "code",
                    docNum.getValue().toLowerCase().trim(), Long.class);

            Predicate customerPredicate = createCustomerPredicate(root, builder);

            Predicate statusPredicate = createStatusPredicate(root, builder);

            return builder.and(predicateCode, customerPredicate, statusPredicate);
        };
    }

    private Predicate createCustomerPredicate(Root<Invoice> root, CriteriaBuilder builder) {
        return createPredicateForSelectedItems(Optional.ofNullable(customer.getSelectedItems()),
                items -> root.get("client").in(items), builder);
    }

    private Predicate createStatusPredicate(Root<Invoice> root, CriteriaBuilder builder) {
        return createPredicateForSelectedItems(Optional.ofNullable(status.getSelectedItems()),
                items -> root.get("status").in(items), builder);
    }

    private Predicate createDatePredicate(Root<Invoice> root, CriteriaBuilder builder, LocalDate from, LocalDate to) {
        return builder.between(root.get("issuedDate"), from, to);
    }

    private void refreshAll() {
        grid.getDataProvider().refreshAll();
        quantity.setText("Facturas (%s)".formatted(service.getCount(createFilterSpecification())));
    }

    private Component createFilter() {
        docNum.focus();
        docNum.setClearButtonVisible(true);
        docNum.setPlaceholder("Enter invoice #");
        docNum.setPrefixComponent(LumoIcon.SEARCH.create());
        docNum.setValueChangeMode(ValueChangeMode.EAGER);
        docNum.addValueChangeListener(e -> refreshAll());

        quantity.addClassNames(LumoUtility.BorderRadius.SMALL, LumoUtility.Height.XSMALL, LumoUtility.FontWeight.MEDIUM,
                LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER, LumoUtility.Padding.XSMALL,
                LumoUtility.Padding.Horizontal.SMALL, LumoUtility.Margin.Horizontal.SMALL,
                LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.Background.PRIMARY, LumoUtility.Display.HIDDEN,
                LumoUtility.Display.Breakpoint.Large.FLEX);
        quantity.setMinWidth(10, Unit.REM);
        quantity.setText("Facturas (%s)".formatted(service.getCount(createFilterSpecification())));

        customer.setItemLabelGenerator(p -> "%s (%s)".formatted(p.getFirstName(), p.getEmail()));

        status.setItems(InvoiceStatus.values());
        status.setItemLabelGenerator(InvoiceStatus::getDisplay);
        //        status.setClassNameGenerator((item) -> switch (item) {
        //            case DRAFT -> "draft";
        //            case UNSENT -> "unsent";
        //            case SENT -> "sent";
        //            case PENDING -> "pending";
        //            case PARTIAL -> "partial";
        //            case PAID -> "paid";
        //            case OVERPAID -> "overpaid";
        //            case OVERDUE -> "overdue";
        //        });

        Set.of(fromPeriod, toPeriod).forEach(c -> {
            c.setValue(LocalDate.now());
            c.setVisible(false);
            c.addValueChangeListener(e -> refreshAll());
        });
        Span periodDiv = new Span("-");
        periodDiv.setVisible(false);

        Set.of(customer, status).forEach(c -> {
            c.setWidthFull();
            c.setClearButtonVisible(true);
            c.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
            c.addValueChangeListener(e -> refreshAll());
        });

        HorizontalLayout toolbar = new HorizontalLayout(docNum, customer, status, fromPeriod, periodDiv, toPeriod,
                quantity);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Margin.Top.SMALL,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.MEDIUM);

        return toolbar;
    }

    private Div createTitle() {
        final Breadcrumb breadcrumb = new Breadcrumb();

        breadcrumb.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        breadcrumb.add(new BreadcrumbItem("Transacciones", InvoiceView.class),
                new BreadcrumbItem("Facturas", InvoiceView.class));

        Icon icon = InfoIcon.INFO_CIRCLE.create("Visualizar y gestionar las facturas de tus clientes.");

        Div headerLayout = new Div(breadcrumb, icon);
        headerLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW,
                LumoUtility.Margin.Top.SMALL);

        create.setText("Crear Factura");
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
        create.addClassNames(LumoUtility.Width.AUTO);

        Div layout = new Div(headerLayout, create);
        layout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.FlexDirection.Breakpoint.Large.ROW, LumoUtility.JustifyContent.BETWEEN,
                LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Margin.Top.SMALL, LumoUtility.Gap.XSMALL,
                LumoUtility.AlignItems.STRETCH, LumoUtility.AlignItems.Breakpoint.Large.END);

        return layout;
    }

    private void generateInvoice(Invoice invoice) {
        var reportService = reportFactory.getServiceFromDatabase("/report/Invoice.jasper");

        InputStream resourceAsStream = InvoiceView.class.getResourceAsStream("/report/your-logo.png");
        log.info("Logo path: {}", resourceAsStream == null ? "null" : "exists");

        reportService.put("INVOICE_ID", invoice.getCode());
        reportService.put("LOGO", resourceAsStream);
    }

    private Div createBoardCards() {
        Section section = new Section(boardCards);
        section.addClassNames(LumoUtility.Width.AUTO, LumoUtility.BorderRadius.LARGE, "md:divide-x");
        section.getElement().setAttribute("aria-label", "statistics");

        Div layout = new Div(section);
        layout.addClassNames(LumoUtility.Width.FULL, LumoUtility.Display.FLEX, LumoUtility.Margin.Horizontal.AUTO,
                LumoUtility.BoxSizing.BORDER, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.LARGE);

        return layout;
    }

    private void recreateBoardCards() {
        boardCards.removeAll();

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        DecimalFormat numberFormat = new DecimalFormat("#,##0");

        BoardCard card = createBoardCard("Abonado", decimalFormat.format(0.00), "DOP");
        boardCards.add(card);

        card = createBoardCard("Deuda a pagar dentro de los próximos 30 días", decimalFormat.format(0.00), "DOP");
        boardCards.add(card);

        card = createBoardCard("Tiempo promedio para recibir pago", numberFormat.format(0), "días");
        boardCards.add(card);
    }

    private BoardCard createBoardCard(String title, String value, String subValue) {

        BoardCard boardCard = new BoardCard(title, value);

        boardCard.setSuffix(createBoardCardSuffix(subValue));

        return boardCard;
    }

    private Component createBoardCardSuffix(String subValue) {

        Span unitText = new Span(subValue);
        unitText.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.Display.INLINE);
        return unitText;
    }

    private Component renderStatus(Invoice item) {
        InvoiceStatus value = item.getStatus();
        Span badge = new Span(value.getDisplay());
        badge.setMinWidth(5, Unit.REM);
        badge.getElement().getThemeList().add("badge pill");

        switch (value) {
        case DRAFT -> badge.getElement().getThemeList().add("contrast");
        case UNSENT -> badge.getElement().getThemeList().add("warning");
        case SENT -> badge.getElement().getThemeList().add("primary");
        case PENDING -> badge.getElement().getThemeList().add("warning");
        case PARTIAL -> badge.getElement().getThemeList().add("contrast");
        case PAID -> badge.getElement().getThemeList().add("success");
        case OVERPAID -> badge.getElement().getThemeList().add("primary");
        case OVERDUE -> badge.getElement().getThemeList().add("error");
        }
        return badge;
    }

    private Component renderActions(Invoice item) {
        Button more = new Button("Más");
        more.addClassNames(LumoUtility.Width.AUTO);
        more.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button edit = new Button("Editar");
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        edit.addClickListener(event -> invoiceForm.edit(item));

        Button duplicate = new Button("Duplicate");
        duplicate.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        Button sendInvoice = new Button("Enviar factura");
        sendInvoice.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        Button exportPDF = new Button("Exportar como PDF");
        exportPDF.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        exportPDF.addClickListener(e -> generateInvoice(item));

        Button print = new Button("Imprimir");
        print.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        print.addClickListener(e -> generateInvoice(item));

        Button delete = new Button("Borrar");
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        Div buttonLayout = new Div(edit, duplicate, new Hr(), sendInvoice, new Hr(), exportPDF, print, delete);
        buttonLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        Popover popover = new Popover();
        popover.setModal(true);
        popover.setBackdropVisible(true);
        popover.setTarget(more);
        popover.add(buttonLayout);

        Div actions = new Div(more);
        actions.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER);
        actions.addClassNames("-mx-s");
        return actions;
    }
}
