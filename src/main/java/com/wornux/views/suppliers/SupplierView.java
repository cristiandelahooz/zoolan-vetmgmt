package com.wornux.views.suppliers;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.components.Breadcrumb;
import com.wornux.components.BreadcrumbItem;
import com.wornux.components.InfoIcon;
import com.wornux.data.entity.Supplier;
import com.wornux.dto.request.SupplierCreateRequestDto;
import com.wornux.services.interfaces.SupplierService;
import com.wornux.utils.GridUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;

@Slf4j
@Route(value = "proveedores")
@PageTitle("Proveedores")
public class SupplierView extends Div {

    //private final Grid<Supplier> grid = new Grid<>(Supplier.class);
    private final Grid<Supplier> grid = new Grid<>();

    private final TextField searchField = new TextField("Buscar proveedores");
    private final Span quantity = new Span();
    private final Button create = new Button();
    private final SupplierService supplierService;
    private final SupplierForm supplierForm;

    public SupplierView(@Qualifier("supplierServiceImpl") SupplierService supplierService) {
        this.supplierService = supplierService;
        this.supplierForm = new SupplierForm(supplierService);

        setId("supplier-view");

        supplierForm.setOnSaveCallback(() -> {
            refreshAll();
            supplierForm.close();
        });

        setSizeFull();
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        createGrid();

        Div gridLayout = new Div(grid);
        gridLayout.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Padding.SMALL, LumoUtility.Height.FULL);

        add(createTitle(), createFilter(), gridLayout);

        create.addClickListener(e -> supplierForm.openForCreate());
    }

    private void createGrid() {
        GridUtils.configureGrid(grid, createFilterSpecification(), supplierService.getRepository());

        GridUtils.addColumn(grid, Supplier::getCompanyName, "Empresa");
        GridUtils.addColumn(grid, Supplier::getContactPerson, "Contacto");
        GridUtils.addColumn(grid, Supplier::getContactPhone, "Teléfono");
        GridUtils.addColumn(grid, Supplier::getContactEmail, "Email");


        GridUtils.addColumn(grid, supplier -> supplier.isActive() ? "Activo" : "Inactivo", "Estado")
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.asSingleSelect().addValueChangeListener(event -> {
            Supplier selected = event.getValue();
            if (selected != null) {
                SupplierCreateRequestDto dto = supplierService.getCreateDtoById(selected.getId());
                supplierForm.openForEdit(dto, selected.getId());
            }
        });
    }

    private Specification<Supplier> createFilterSpecification() {
        return (root, query, builder) -> {
            Predicate predicate = builder.conjunction();

            if (!searchField.isEmpty()) {
                String value = "%" + searchField.getValue().toLowerCase() + "%";
                Predicate name = builder.like(builder.lower(root.get("companyName")), value);
                Predicate contact = builder.like(builder.lower(root.get("contactPerson")), value);
                Predicate email = builder.like(builder.lower(root.get("contactEmail")), value);
                predicate = builder.or(name, contact, email);
            }

            //query.orderBy(builder.desc(root.get("createdAt")));
            return predicate;
        };
    }

    private Component createFilter() {
        searchField.setClearButtonVisible(true);
        searchField.setPlaceholder("Buscar por empresa, contacto o email...");
        searchField.setPrefixComponent(LumoIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> refreshAll());

        quantity.addClassNames(LumoUtility.BorderRadius.SMALL, LumoUtility.Height.XSMALL,
                LumoUtility.FontWeight.MEDIUM, LumoUtility.JustifyContent.CENTER,
                LumoUtility.AlignItems.CENTER, LumoUtility.Padding.XSMALL,
                LumoUtility.Padding.Horizontal.SMALL, LumoUtility.Margin.Horizontal.SMALL,
                LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.Background.PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, quantity);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Margin.Top.SMALL,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.MEDIUM);

        refreshAll();

        return toolbar;
    }

    private Div createTitle() {
        final Breadcrumb breadcrumb = new Breadcrumb();

        breadcrumb.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        breadcrumb.add(new BreadcrumbItem("Proveedores", SupplierView.class));

        Icon icon = InfoIcon.INFO_CIRCLE.create("Gestionar proveedores activos e inactivos.");

        Div headerLayout = new Div(breadcrumb, icon);
        headerLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW,
                LumoUtility.Margin.Top.SMALL);

        create.setText("Nuevo Proveedor");
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
        create.addClassNames(LumoUtility.Width.AUTO);

        Div layout = new Div(headerLayout, create);
        layout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.FlexDirection.Breakpoint.Large.ROW, LumoUtility.JustifyContent.BETWEEN,
                LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Margin.Top.SMALL,
                LumoUtility.Gap.XSMALL, LumoUtility.AlignItems.STRETCH,
                LumoUtility.AlignItems.Breakpoint.Large.END);

        return layout;
    }

    private void refreshAll() {
        grid.getDataProvider().refreshAll();
        long count = supplierService.getAllSuppliers().stream().filter(Supplier::isActive).count();
        quantity.setText("Proveedores Activos (" + count + ")");
    }
}
