package com.wornux.views.transactions;

import com.wornux.components.*;
import com.wornux.data.entity.Product;
import com.wornux.services.implementations.ProductServiceImpl;
import com.wornux.utils.GridUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.wornux.utils.PredicateUtils.predicateUnaccentLike;

@Slf4j
@Uses(Icon.class)
@Route(value = "products")
@RolesAllowed({ "ADMIN" })
@PageTitle("Products & Services Management")
public class ProductServiceView extends Div {
    private final Button create = new Button("Create", LineAwesomeIcon.ROCKET_SOLID.create());

    private final Grid<Product> grid = GridUtils.createBasicGrid(Product.class);

    private final QuantityComponent quantity = new QuantityComponent();
    private final SearchComponent searchField = new SearchComponent();

    private final ProductServiceImpl service;
    private final ProductServiceForm productServiceForm;

    public ProductServiceView(ProductServiceImpl service) {
        this.service = service;

        productServiceForm = new ProductServiceForm(service);

        createGrid();

        final Div gridLayout = new Div(grid);
        gridLayout.addClassNames(LumoUtility.Margin.MEDIUM, LumoUtility.Height.FULL);

        add(createTitle(), createFilter(), gridLayout, productServiceForm);
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
        setSizeFull();

        productServiceForm.setCallable(this::refreshAll);

        create.addClickListener(event -> productServiceForm.open());
    }

    private static Div getDiv(Breadcrumb breadcrumb) {
        Icon icon = InfoIcon.INFO_CIRCLE.create(
                "View and manage products & services information. The Products & services that you sell to customers are used as items on Invoices to record those sales.");

        Div headerLayout = new Div(breadcrumb, icon);
        headerLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW,
                LumoUtility.Margin.Top.SMALL);
        return headerLayout;
    }

    private Specification<Product> createFilterSpecification() {
        return (root, query, builder) -> {
            final String search = searchField.getValue().toLowerCase().trim();

            Order order = builder.asc(root.get("name"));
            assert query != null;
            query.orderBy(order);
            query.distinct(true);

            Predicate predicateName = predicateUnaccentLike(root, builder, "name", search);
            Predicate predicateDescription = predicateUnaccentLike(root, builder, "description", search);

            final List<Predicate> orPredicates = new ArrayList<>(List.of(predicateName, predicateDescription));
            Predicate orPredicate = orPredicates.isEmpty() ? builder.conjunction() : builder.or(
                    orPredicates.toArray(Predicate[]::new));

            Predicate deleted = builder.isFalse(root.get("deleted"));

            return builder.and(orPredicate, deleted);
        };
    }

    private void createGrid() {
        grid.setDetailsVisibleOnClick(false);

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                productServiceForm.edit(event.getValue());
            } else {
                productServiceForm.close();
            }
        });

        GridUtils.configureGrid(grid, createFilterSpecification(), service.getProductRepository());

        GridUtils.addColumn(grid, Product::getName, "Name", "name");

        GridUtils.addColumn(grid, Product::getDescription, "Description", "description");

        GridUtils.addColumn(grid, c -> new DecimalFormat("#,##0.00").format(c.getPrice()), "Price", "price")
                .setTextAlign(ColumnTextAlign.END);

    }

    private Component createFilter() {
        searchField.focus();
        searchField.addValueChangeListener(e -> refreshAll());
        quantity.setText("Products (%s)".formatted(service.getCount(createFilterSpecification())));

        Div toolbar = new Div(searchField, quantity);
        toolbar.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW,
                LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Gap.Column.SMALL);

        return toolbar;
    }

    private Div createTitle() {
        Breadcrumb breadcrumb = new Breadcrumb();
        breadcrumb.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        //        breadcrumb.add(new BreadcrumbItem("Home", HomeView.class),
        //                new BreadcrumbItem("Transactions", ProductServiceView.class),
        //                new BreadcrumbItem("Products & Services", ProductServiceView.class));

        Div headerLayout = getDiv(breadcrumb);

        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        create.addClassNames(LumoUtility.Margin.Top.AUTO, LumoUtility.AlignSelf.END);

        Div layout = new Div(headerLayout, create);
        layout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.FlexDirection.Breakpoint.Medium.ROW, LumoUtility.Padding.SMALL, LumoUtility.Gap.MEDIUM,
                LumoUtility.JustifyContent.BETWEEN, LumoUtility.Margin.Horizontal.MEDIUM,
                LumoUtility.Position.RELATIVE);

        return layout;
    }

    private void refreshAll() {
        grid.getDataProvider().refreshAll();
        quantity.setText("Products (%s)".formatted(service.getCount(createFilterSpecification())));
    }

}
